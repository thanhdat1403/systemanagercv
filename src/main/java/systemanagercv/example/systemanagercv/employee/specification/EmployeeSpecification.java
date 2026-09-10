package systemanagercv.example.systemanagercv.employee.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import systemanagercv.example.systemanagercv.employee.dto.request.EmployeeSearchRequest;
import systemanagercv.example.systemanagercv.employee.entity.Employee;

import java.util.ArrayList;
import java.util.List;

// Khai báo final và private constructor để ngăn việc khởi tạo đối tượng (Utility Class)
public final class EmployeeSpecification {

    public EmployeeSpecification() {
    }

    /**
     * Hàm chính để tạo ra cấu trúc tìm kiếm động dựa trên dữ liệu lọc nhân viên gửi lên (EmployeeSearchRequest)
     */
    public static Specification<Employee> search(
            EmployeeSearchRequest request
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
            // Nếu người dùng có chọn một phòng ban cụ thể trên giao diện
            if (request != null
                    && request.getDepartmentId() != null) {
                // Đi từ đối tượng Employee gốc -> Lấy thuộc tính liên kết "department" -> Lấy tiếp id để so sánh
                //Tương đương với SQL: AND department_id = :departmentId
                predicates.add(
                        cb.equal(
                                root.get("department").get("id"),
                                request.getDepartmentId()
                        )
                );
            }

            // =========================================================================
            // 4. ĐIỀU KIỆN LỌC THEO CHỨC VỤ (Position)
            // =========================================================================
            // Nếu người dùng có chọn lọc theo chức vụ (Ví dụ: DEVELOPER, MANAGER...)
            if (request != null
                    && request.getPosition() != null) {
                //Tương đương SQL: AND position = :position
                predicates.add(
                        cb.equal(
                                root.get("position"),
                                request.getPosition()
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