package systemanagercv.example.systemanagercv.cv.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import systemanagercv.example.systemanagercv.cv.dto.request.CVCreateRequest;
import systemanagercv.example.systemanagercv.cv.dto.request.CVSearchRequest;
import systemanagercv.example.systemanagercv.cv.dto.response.CVDetailResponse;
import systemanagercv.example.systemanagercv.cv.dto.response.CVListResponse;
import systemanagercv.example.systemanagercv.cv.mapper.CVMapper;
import systemanagercv.example.systemanagercv.cv.repository.EmployeeCVRepository;
import systemanagercv.example.systemanagercv.cv.service.CVService;
import systemanagercv.example.systemanagercv.employee.authorization.EmployeeAccessScope;
import systemanagercv.example.systemanagercv.employee.authorization.EmployeeAccessType;
import systemanagercv.example.systemanagercv.employee.authorization.EmployeeAuthorizationService;
import systemanagercv.example.systemanagercv.employee.repository.EmployeeRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class CVServiceImpl implements CVService {
    /*Tại sao cần 4 dependency này:
    * EmployeeCVRepository dùng để:
    * CV tồn tại?
        Tạo CV
        Tìm CV
        Tìm danh sách CV
     * EmployeeRepository dùng để: Employee employee = employeeRepository.findById(request.getEmployeeId())
     * CVMapper dùng đúng theo convention (K map trong Controller): Request → Entity
                                           Entity/Projection → Response
     * EmployeeAuthorizationService (k tạo lại logic): ADMIN, HR, TECH_LEAD, EMPLOYEE mà tái sử dụng employeeAuthorizationService.getCurrentUserScope()*/
    private final EmployeeCVRepository employeeCVRepository;
    private final EmployeeRepository employeeRepository;
    private final CVMapper cvMapper;
    private final EmployeeAuthorizationService employeeAuthorizationService;

    // Tạo CV
    @Override
    public CVDetailResponse create(CVCreateRequest cvCreateRequest) {
        return null;
    }

    // Tìm kiếm + phân trang, sắp xếp
    @Override
    @Transactional(readOnly = true)
    public Page<CVListResponse> search(CVSearchRequest request) {

        // 1. Khởi tạo bộ cấu hình phân trang và sắp xếp (Page, Size, SortBy, SortDirection) từ request gửi lên.
        Pageable pageable = createPageable(request);

        // 2. Lấy ra "Phạm vi quyền hạn" (Scope) của tài khoản đang đăng nhập hiện tại trong hệ thống.
        EmployeeAccessScope accessScope =
                employeeAuthorizationService.getCurrentUserScope();

        // Khởi tạo hai biến chốt chặn quyền hạn mặc định là null (tương đương với quyền ADMIN - xem tất cả)
        Long scopeDepartmentId = null;
        Long scopeEmployeeId  = null;

        // 3. Phân luồng kiểm tra quyền hạn (Bảo mật ở tầng Service):

        // Nếu người đăng nhập có quyền cấp PHÒNG BAN (Ví dụ: Trưởng phòng):
        if (accessScope.getAccessType() == EmployeeAccessType.DEPARTMENT){
            scopeDepartmentId = accessScope.getDepartmentId(); // Gán ID phòng ban của họ vào bộ lọc
        }

        // Nếu người đăng nhập chỉ có quyền CÁ NHÂN (Ví dụ: Nhân viên thường):
        if (accessScope.getAccessType() == EmployeeAccessType.SELF){
            scopeEmployeeId = accessScope.getEmployeeId();
        }

        // 4. Chuẩn hóa và làm sạch từ khóa tìm kiếm (gọt khoảng trắng, biến chuỗi rỗng thành null)
        String keyword = normalizeKeyword(request.getKeyword());

        // 5. Thọc xuống tầng Repository để bắn câu lệnh SQL thông minh xuống Database
        return employeeCVRepository
                .searchActiveForList(
                        keyword,                    // Từ khóa đã làm sạch
                        request.getDepartmentId(),  // ID phòng ban muốn lọc (nếu có)
                        request.getStatus(),        // Trạng thái CV muốn lọc (nếu có)
                        scopeDepartmentId,          // Chốt chặn ID phòng ban theo quyền (nếu có)
                        scopeEmployeeId,            // Chốt chặn ID nhân viên theo quyền (nếu có)
                        pageable                    // Bộ cấu hình phân trang/sắp xếp
                )
                // 6. Sau khi DB trả về danh sách Entity (EmployeeCV), dùng cvMapper để "đóng gói"
                // chuyển đổi (Mapping) chúng sang dạng DTO (CVListResponse) trước khi trả về cho Controller.
                .map(cvMapper::toListResponse);
    }


    // lấy chi tiết CV
    @Override
    public CVDetailResponse getDetail(Long id) {
        return null;
    }

    /**
     * Hàm hỗ trợ tự động tạo đối tượng Phân trang và Sắp xếp (Pageable) từ Request của Client gửi lên.
     *
     * @param request Đối tượng chứa các tham số tìm kiếm, phân trang từ giao diện (Page, Size, SortBy...)
     * @return Pageable Đối tượng chuẩn của Spring để truyền vào tầng Repository truy vấn DB
     */
    private Pageable createPageable(CVSearchRequest request) {

        // 1. Phân tích cột cần sắp xếp: Chuyển đổi tên trường từ giao diện gửi lên (Ví dụ: "name")
        // sang đúng tên thuộc tính trong file Entity Java (ví dụ: "fullName").
        String sortProperty = resolveSortProperty(request.getSortBy());

        // 2. Phân tích hướng sắp xếp: chuyển đổi chuỗi chữ "asc" hoặc "desc" từ request
        // thành đối tượng Enum Sort.Direction (ASC: tăng dần, DESC: giảm dần) của Spring.
        Sort.Direction direction =
                Sort.Direction.fromString(request.getSortDirection());

        // 3. Khởi tạo và trả về đối tượng PageRequest (triển khai từ interface Pageable)
        // chứa 3 tham số cốt lõi: Số trang muốn xem, Số dòng trên mỗi trang, và Quy tắc sắp xếp.
        return PageRequest.of(
                request.getPage(),                 // Số thứ tự trang cần lấy (Bắt đầu từ số 0)
                request.getSize(),                 // Số lượng dòng dữ liệu hiển thị trên 1 trang (ví dụ: 10, 20 dòng) đã set cứng trong DTO Request cụ thể ở CVSearchRequest
                Sort.by(direction, sortProperty)   // Quy tắc sắp xếp: Hướng sắp xếp + Cột sắp xếp
        );
    }

    /**
     * Hàm hỗ trợ kiểm tra và chuyển đổi tên cột cần sắp xếp từ giao diện gửi lên
     * thành đúng tên thuộc tính (Field) nằm trong câu lệnh truy vấn JPQL/Entity.
     *
     * @param sortBy Tên cột cần sắp xếp do Client gửi lên (ví dụ: "employeeName")
     * @return String Tên thuộc tính chuẩn xác để đưa vào bộ lọc của Spring Data JPA
     */
    private String resolveSortProperty(String sortBy) {

        // 1. Kiểm tra phòng hờ: Nếu người dùng không truyền cột cần sắp xếp (null hoặc để trống "")
        // thì hệ thống sẽ tự động lấy cột mặc định là "id" để sắp xếp
        if (sortBy == null || sortBy.isBlank()) {
            return "id";
        }

        // 2. Sử dụng cấu trúc Switch-Expression (Java 14+) để đối chiếu từ khóa:
        // Cứ khớp với chữ nào ở vế trái (Client gửi), sẽ trả về đúng chữ ở vế phải (Database nhận).
        return switch (sortBy) {
            case "id" -> "id";                           // Sắp xếp theo ID của CV
            case "status" -> "status";                   // Sắp xếp theo Trạng thái của CV

            // 3. Chốt chặn an toàn (Whitelisting): Nếu người dùng cố tình truyền lên một từ khóa lạ hoắc
            // không nằm trong danh sách trên (ví dụ: "password", "salary"), hệ thống sẽ bỏ qua
            // và tự động quay về sắp xếp theo cột "id", giúp ứng dụng KHÔNG BỊ SẬP (Lỗi SQL).
            default -> "id";
        };
    }

    /**
     * Hàm hỗ trợ kiểm tra và làm sạch từ khóa tìm kiếm (Keyword) từ giao diện gửi lên.
     * Đảm bảo từ khóa đạt tiêu chuẩn trước khi đưa vào câu lệnh truy vấn SQL/JPQL.
     *
     * @param keyword Từ khóa thô do người dùng nhập vào ô tìm kiếm (ví dụ: "  NV01   ")
     * @return String Từ khóa đã được làm sạch, hoặc null nếu người dùng không nhập gì
     */
    private String normalizeKeyword(String keyword) {

        // 1. Kiểm tra phòng hờ: Nếu người dùng không nhập từ khóa (null), hoặc chỉ gõ toàn dấu cách
        // khoảng trắng mà không có chữ ("   "), hàm sẽ lập tức trả về giá trị null.
        // Việc trả về null giúp câu lệnh SQL hiểu rằng "Bộ lọc từ khóa này được BỎ QUA".
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        // 2. Nếu từ khóa hợp lệ: Hàm sử dụng lệnh .trim() để tự động "gọt bỏ" tất cả các khoảng trắng
        // dư thừa ở hai đầu (đầu và cuối) của chuỗi văn bản.
        return keyword.trim();
    }
}
