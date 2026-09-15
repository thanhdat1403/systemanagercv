package systemanagercv.example.systemanagercv.employee.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import systemanagercv.example.systemanagercv.employee.authorization.EmployeeAccessScope;
import systemanagercv.example.systemanagercv.employee.authorization.EmployeeAccessType;
import systemanagercv.example.systemanagercv.employee.dto.request.EmployeeSearchRequest;
import systemanagercv.example.systemanagercv.employee.entity.Employee;

import java.util.ArrayList;
import java.util.List;

// Khai báo final và private constructor để ngăn việc khởi tạo đối tượng (Utility Class)
public final class EmployeeSpecification {

    private EmployeeSpecification() {
    }

    /**
     * Hàm chính để tạo ra cấu trúc tìm kiếm động dựa trên dữ liệu lọc nhân viên gửi lên (EmployeeSearchRequest)
     */
    public static Specification<Employee> search(
            EmployeeSearchRequest request,
            EmployeeAccessScope accessScope
    ){
        // root: Đại diện cho bảng Employee (đối tượng gốc để lấy các trường dữ liệu)
        // query: Đại diện cho toàn bộ câu lệnh truy vấn (giúp cấu hình DISTINCT, ORDER BY,...)
        // cb (criteriaBuilder): Công cụ để xây dựng các điều kiện so sánh (LIKE, EQUAL, AND, OR,...)
        return (root, query, cb) ->  {

            //Danh sách chứa tất cả các điều kiện lọc (WHERE) sẽ được kết hợp bằng  toán tử AND
            List<Predicate> predicates = new ArrayList<>();

            // =========================================================================
            // 1. ĐIỀU KIỆN MẶC ĐỊNH: Không lấy Employee đã bị xóa mềm (Soft Delete)
            // =========================================================================
            // Tương đương trong SQL: WHERE deleted = false
            predicates.add(
                    cb.isFalse(root.get("deleted"))
            );

            // =========================================================================
            // 2. ĐIỀU KIỆN TÌM KIẾM THEO TỪ KHÓA (Keyword)
            // =========================================================================
            // Nếu người dùng có nhập từ khóa (không bị null và không phải khoảng trắng)
            if (request != null
                    && request.getKeyword() != null
                    && !request.getKeyword().isBlank()) {

                //Chuẩn hóa từ khóa: xóa khoảng trắng, chuyển về chữ thường và bọc trong dấu % để tìm kiếm chứa chuỗi (LIKE)
                // Ví dụ: " EMP01 " -> "%emp01%"
                String keyword = "%" +
                        request.getKeyword().trim().toLowerCase() + "%";

                //Tạo điều kiện 2.1: Tìm theo Mã nhân viên (Không phân biệt chữ hoa/thường nhờ cb.lower)
                Predicate employeeCode = cb.like(
                        cb.lower(root.get("employeeCode")),
                        keyword
                );

                //Tạo điều kiện 2.2: Tìm theo Họ và tên (Không phân biệt chữ hoa/thường)
                Predicate fullName = cb.like(
                        cb.lower(root.get("fullName")),
                        keyword
                );

                //Tạo điều kiện 2.3: Tìm theo Email nhân viên (Không phân biệt chữ hoa/thường)
                Predicate email = cb.like(
                        cb.lower(root.get("email")),
                        keyword
                );

                //Gộp 3 điều kiện trên bằng toán tử OR (Tìm thấy ở Mã NV HOẶC Tên HOẶC Email đều được)
                // Tương đương SQL: AND (LOWER(employee_code) LIKE ... OR LOWER(full_name) LIKE ... OR LOWER(email) LIKE ...)
                predicates.add(
                        cb.or(
                                employeeCode,
                                fullName,
                                email
                        )
                );
            }

            // =========================================================================
            // 3. ĐIỀU KIỆN LỌC THEO PHÒNG BAN (Department)
            // =========================================================================
            // Kiểm tra: Nếu hệ thống có cấu hình giới hạn phạm vi truy cập cho tài khoản này
            if (accessScope != null) {

                /*
                 * TRƯỜNG HỢP 1: TÀI KHOẢN CHỈ CÓ QUYỀN TRONG PHẠM VI PHÒNG BAN (DEPARTMENT)
                 * (Ví dụ: Bạn là Tech Lead hoặc HR Manager, bạn chỉ được xem nhân viên của phòng mình).
                 */
                if (accessScope.getAccessType() == EmployeeAccessType.DEPARTMENT) {

                    // Chốt chặn an toàn: Nếu hệ thống bị lỗi không tìm thấy ID phòng ban của người này
                    if (accessScope.getDepartmentId() == null) {
                        // cb.disjunction() tương đương lệnh SQL: "WHERE 1=0" (Ép mệnh đề luôn sai)
                        // Mục đích: Chặn đứng không cho trả về bất kỳ dòng dữ liệu nào để bảo mật thông tin.
                        predicates.add(cb.disjunction());
                    } else {
                        // Ngược lại, nếu có ID phòng ban hợp lệ -> Tự động cấy thêm lệnh SQL ngầm:
                        // "AND department_id = [ID phòng ban của chính người đang đăng nhập]"
                        predicates.add(
                                cb.equal(
                                        root.get("department").get("id"), // Mò vào cột ID phòng ban của bảng dữ liệu đang quét
                                        accessScope.getDepartmentId()     // Đối chiếu với ID phòng ban của người xem
                                )
                        );
                    }

                    /*
                     * 👤 TRƯỜNG HỢP 2: TÀI KHOẢN CHỈ CÓ QUYỀN TRONG PHẠM VI CÁ NHÂN (SELF)
                     * (Ví dụ: Bạn là Nhân viên thường, bạn chỉ được quyền xem duy nhất Hồ sơ CV của chính bạn).
                     */
                } else if (accessScope.getAccessType() == EmployeeAccessType.SELF) {

                    // Chốt chặn an toàn: Nếu không tìm thấy ID nhân viên của tài khoản đang đăng nhập
                    if (accessScope.getEmployeeId() == null) {
                        // Chặn đứng, trả về danh sách trống rỗng để bảo vệ an toàn hệ thống
                        predicates.add(cb.disjunction());
                    } else {
                        // Nếu có ID nhân viên hợp lệ -> Tự động cấy thêm lệnh SQL ngầm:
                        // "AND id = [ID nhân viên của chính người đang đăng nhập]"
                        predicates.add(
                                cb.equal(
                                        root.get("id"),               // Mò vào cột ID khóa chính của bảng dữ liệu đang quét
                                        accessScope.getEmployeeId()   // Đối chiếu với ID cá nhân của người xem
                                )
                        );
                    }
                }

                /*
                 * 💡 MẸO HIỂU NGẦM: Nếu 'accessScope.getAccessType()' mang giá trị là "ALL" (Quyền Admin tối cao),
                 * code sẽ tự động bỏ qua cả 2 nhánh 'if' trên, không cấy thêm điều kiện chặn nào cả,
                 * giúp Admin có thể nhìn thấy toàn bộ dữ liệu của tất cả mọi người trong công ty!
                 */
            }

            // =========================================================================
            // 4. ĐIỀU KIỆN LỌC THEO PHÒNG BAN DO NGƯỜI DÙNG YÊU CẦU
            // =========================================================================
            // Đây là bộ lọc departmentId được gửi trực tiếp từ request.
            //
            // Ví dụ:
            // GET /api/v1/employees?departmentId=1
            //
            // Tương đương:
            // AND department_id = 1
            //
            // Lưu ý:
            // Điều kiện này chỉ LỌC thêm dữ liệu.
            // Nó không thể mở rộng quyền truy cập của người dùng.
            // Authorization scope ở phía trên vẫn được áp dụng độc lập.

            if (request != null
                    && request.getDepartmentId() != null) {

                predicates.add(
                        cb.equal(
                                root.get("department").get("id"),
                                request.getDepartmentId()
                        )
                );
            }

            // =========================================================================
            // 5. ĐIỀU KIỆN LỌC THEO TRẠNG THÁI LÀM VIỆC (Status)
            // =========================================================================
            // Nếu người dùng có chọn lọc theo trạng thái (Ví dụ: ACTIVE, INACTIVE, LEAVE...)
            if (request != null
                    && request.getStatus() != null) {
                //Tương đương SQL: AND status = :status
                predicates.add(
                        cb.equal(
                                root.get("status"),
                                request.getStatus()
                        )
                );
            }

            // Gộp tất cả các điều kiện trong danh sách thành một câu lệnh có dạng:
            // WHERE deleted = false AND (LOWER(...) LIKE ...) AND department_id = ... AND ...
            return cb.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}
/*Mục tiêu xử lý:
* EmployeeSearchRequest
       ↓
EmployeeSpecification
       ↓
WHERE
    deleted = false
    AND keyword...
    AND department...
    AND position...
    AND status...*/