package systemanagercv.example.systemanagercv.employee.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import systemanagercv.example.systemanagercv.common.enums.RoleName;
import systemanagercv.example.systemanagercv.common.exception.BusinessException;
import systemanagercv.example.systemanagercv.department.entity.Departments;
import systemanagercv.example.systemanagercv.department.service.DepartmentService;
import systemanagercv.example.systemanagercv.employee.dto.request.EmployeeCreateRequest;
import systemanagercv.example.systemanagercv.employee.dto.request.EmployeeSearchRequest;
import systemanagercv.example.systemanagercv.employee.dto.request.EmployeeUpdateRequest;
import systemanagercv.example.systemanagercv.employee.dto.response.EmployeeDetailResponse;
import systemanagercv.example.systemanagercv.employee.dto.response.EmployeeResponse;
import systemanagercv.example.systemanagercv.employee.entity.Employee;
import systemanagercv.example.systemanagercv.employee.mapper.EmployeeMapper;
import systemanagercv.example.systemanagercv.employee.repository.EmployeeRepository;
import systemanagercv.example.systemanagercv.employee.specification.EmployeeSpecification;
import systemanagercv.example.systemanagercv.user.entity.User;
import systemanagercv.example.systemanagercv.user.service.UserService;

import java.util.List;
@Service
@RequiredArgsConstructor // Tự động tạo Constructor cho tất cả các Service/Repository được khai báo final bên dưới
/* Thay vì Cách 1: dùng tiêm phụ thuộc = @Autowired: cx đc nhưng hơi rối mắt
* Cách 2: Vt constructor để an toàn hơn, nhưng lại viết dài
*  VD:    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;

    // Phải viết đống code này bằng tay:
    public EmployeeService(EmployeeRepository employeeRepository, ModelMapper modelMapper) {
        this.employeeRepository = employeeRepository;
        this.modelMapper = modelMapper;*/
@Transactional // Bật "tấm lưới bảo hiểm" cho toàn bộ Class: Bất kỳ hàm thêm/sửa/xóa nào bị lỗi giữa chừng đều tự Rollback
public class EmployeeServiceImpl implements EmployeeService {

    // Khai báo các hằng số tên cột dữ liệu an toàn phục vụ cho việc Sắp xếp (Sort)
    private static final String DEFAULT_SORT_PROPERTY = "id";

    private static final String SORT_ID = "id";
    private static final String SORT_EMPLOYEE_CODE = "employeeCode";
    private static final String SORT_FULL_NAME = "fullName";
    private static final String SORT_EMAIL = "email";
    private static final String SORT_JOIN_DATE = "joinDate";

    //Tiêm (Inject) các công cụ phụ trợ cần thiết phục vụ cho việc xử lý logic
        private final EmployeeRepository employeeRepository;
        private final EmployeeMapper employeeMapper;
        private final UserService userService;
        private final DepartmentService departmentService;


    /**
     * ============================================================
     * 1. TÌM KIẾM NÂNG CAO + PHÂN TRANG NHÂN VIÊN
     * ============================================================
     */
    @Override
    @Transactional(readOnly = true) // Hàm chỉ đọc dữ liệu (SELECT) -> giúp tăng tốc độ truy vấn tối đa
    public Page<EmployeeResponse> search(EmployeeSearchRequest request) {

        //Gọi hàm tạo thông số phân trang (hàm createPageable được viết ở cuối file)
        Pageable pageable = createPageable(request);

        //Kết hợp file lọc động EmployeeSpecification và khuôn phân trang để quét Database
        return employeeRepository
                .findAll(EmployeeSpecification.search(request), pageable)
                .map(employeeMapper::toResponse); // Tự động lặp và đổi ruột từ dữ liệu thô sang EmployeeResponse sạch đẹp
    }

    /**
     * ============================================================
     * 2. LẤY THÔNG TIN CHI TIẾT CỦA MỘT NHÂN VIÊN THEO ID
     * ============================================================
     */
    @Override
    @Transactional(readOnly = true) //Chỉ đọc dữ liệu để tối ưu bộ nhớ
    public EmployeeDetailResponse findById(Long id) {

        //Vào bảng tìm kiếm thông tin chi tiết nhân viên theo ID
        // Nếu không tìm thấy hoặc nhân viên đã bị xóa -> Lập tức chặn lại và ném lỗi nghiệp vụ
        Employee employee = employeeRepository
                .findEmployeeDetailById(id)
                .orElseThrow(() ->
                        new BusinessException("error.employee.notFound")
                );

        //Chuyển đổi dữ liệu sang gói DTO chi tiết để trả về cho phía giao diện
        return employeeMapper.toDetailResponse(employee);
    }

    /**
     * ============================================================
     * 3. THÊM MỚI NHÂN VIÊN (logic gồm 8 bước chặt chẽ)
     * ============================================================
     */
    @Override
    public EmployeeResponse create(EmployeeCreateRequest request) {
        /*
         * --------------------------------------------------------
         * Bước 1: Kiểm tra xem Mã nhân viên (employeeCode) có bị trùng không
         * --------------------------------------------------------
         */
        String employeeCode = request.getEmployeeCode().trim(); // xóa khoảng trắng thừa

        //Nếu trong DB đã tồn tại một nhân viên chưa bị xóa có mã trùng -> ném lỗi chặn lại ngay
        if (employeeRepository.existsByEmployeeCodeAndDeletedFalse(employeeCode)) {
            throw new BusinessException("error.employee.employeeCodeExists");
        }

        /*
         * --------------------------------------------------------
         * Bước 2: Lấy thông tin tài khoản (User) được chọn để liên kết
         * --------------------------------------------------------
         */
        // Nhờ UserService tìm ông User đang hoạt động theo ID gửi lên. Nếu không thấy, hàm này tự ném lỗi rồi.
        User user = userService.findActiveUserEntityById(request.getUserId());

        /*
         * --------------------------------------------------------
         * Bước 3: Kiểm tra xem tài khoản User đó có Quyền Nhân Viên hay không
         * --------------------------------------------------------
         */
        // Bật "băng chuyền" duyệt qua danh sách các quyền của User hiện tại,
        // kiểm tra xem có quyền nào trùng khớp với tên "EMPLOYEE" hay không.
        boolean isEmployeeRole = user.getUserRoles()
                .stream()
                .anyMatch(userRole ->
                        RoleName.EMPLOYEE.name()
                                .equals(userRole.getRole().getName())
                );

        //Nếu tài khoản này không có quyền Nhân viên (Ví dụ là quyền ADMIN)
        if (!isEmployeeRole) {
            throw new BusinessException("error.employee.userInvalidRole");
        }

        /*
         * --------------------------------------------------------
         * Bước 4: Kiểm tra xem tài khoản User này đã gắn với nhân viên nào chưa
         * --------------------------------------------------------
         */
        // Quy tắc: 1 tài khoản chỉ được gán cho 1 nhân viên duy nhất.
        // Nếu ID tài khoản này đã được dùng cho một nhân viên khác rồi -> báo lỗi chặn lại
        if (employeeRepository.existsByUserIdAndDeletedFalse(request.getUserId())) {
            throw new BusinessException("error.employee.userAlreadyAssigned");
        }

        /*
         * --------------------------------------------------------
         * Bước 5: Lấy thông tin Phòng ban (Department) của nhân viên
         * --------------------------------------------------------
         */
        // Nhờ DepartmentService đi tìm phòng ban theo ID. Nếu không thấy phòng ban, hệ thống tự báo lỗi.
        Departments department =
                departmentService.findEntityById(request.getDepartmentId());

        /*
         * --------------------------------------------------------
         * Bước 6: Chuyển dữ liệu (Map Request DTO → Entity) và gán quan hệ
         * --------------------------------------------------------
         */
        Employee employee = employeeMapper.toEntity(request);

        employee.setUser(user); // Gắn thực thể User vào Nhân viên
        employee.setDepartment(department); // Gắn phòng ban vào Nhân viên
        employee.setEmployeeCode(employeeCode); // Gắn mã nhân viên sạch đã cắt khoảng trắng

        /*
         * --------------------------------------------------------
         * Bước 7: Chính thức lưu thông tin Nhân viên vào Database
         * --------------------------------------------------------
         */
        Employee savedEmployee = employeeRepository.save(employee);

        /*
         * --------------------------------------------------------
         * Bước 8: Chuyển đổi dữ liệu và trả về (Entity → Response DTO)
         * --------------------------------------------------------
         */
        return employeeMapper.toResponse(savedEmployee);
    }

    /**
     * ============================================================
     * 4. CẬP NHẬT THÔNG TIN NHÂN VIÊN
     * ============================================================
     */
    @Override
    public EmployeeResponse update(
            Long id,
            EmployeeUpdateRequest request
    ) {
        /*
         * --------------------------------------------------------
         * Bước 1: Tìm thông tin Nhân viên hiện tại cần sửa đổi
         * --------------------------------------------------------
         */
        // Tìm nhân viên theo ID, lọc bỏ những người đã bị đánh dấu xóa mềm (.filter(existing -> !existing.isDeleted()))
        // Nếu không thấy -> lập tức ngừng xử lý và ném lỗi "Không tìm thấy nhân viên"
        Employee employee = employeeRepository
                .findById(id)
                .filter(existing -> !existing.isDeleted())
                .orElseThrow(() ->
                        new BusinessException("error.employee.notFound")
                );

        /*
         * --------------------------------------------------------
         * Bước 2: Kiểm tra xem Mã nhân viên mới có bị trùng với người khác không
         * --------------------------------------------------------
         */
        String employeeCode = request.getEmployeeCode().trim();

        //Nếu mã mới này trùng với một ai đó khác (IdNot) trên hệ thống -> Báo lỗi chặn lại ngay
        if (employeeRepository.existsByEmployeeCodeAndIdNotAndDeletedFalse(employeeCode, id)) {
            throw new BusinessException("error.employee.employeeCodeExists");
        }

        /*
         * --------------------------------------------------------
         * Bước 3: Lấy thông tin tài khoản User mới từ hệ thống
         * --------------------------------------------------------
         */
        User user = userService.findActiveUserEntityById(request.getUserId());

        /*
         * --------------------------------------------------------
         * Bước 4: Kiểm tra xem tài khoản User mới đó có đúng quyền EMPLOYEE hay không
         * --------------------------------------------------------
         */
        boolean isEmployeeRole = user.getUserRoles()
                .stream()
                .anyMatch(userRole ->
                        RoleName.EMPLOYEE.name()
                                .equals(userRole.getRole().getName())
                );

        if (!isEmployeeRole) {
            throw new BusinessException("error.employee.userInvalidRole");
        }

        /*
         * --------------------------------------------------------
         * Bước 5: Kiểm tra xem tài khoản User mới này đã được nhân viên khác sử dụng chưa
         * --------------------------------------------------------
         */
        // Kiểm tra xem ID của tài khoản mới nhập có khác (thay ĐỔI) so với ID tài khoản cũ của nhân viên này không
        boolean userChanged =
                !user.getId().equals(employee.getUser().getId());

        //Nếu có THAY ĐỔI tài khoản, VÀ cái tài khoản mới đó lại ĐÃ BỊ gán cho một nhân viên khác mất rồi
        // -> Lập tức chặn đứng lại và báo lỗi "Tài khoản đã đc gán cho nhân viên khác"
        if (userChanged
                && employeeRepository.existsByUserIdAndDeletedFalse(
                        request.getUserId()
        )) {

            throw new BusinessException("error.employee.userAlreadyAssigned");
        }

        /*
         * --------------------------------------------------------
         * Bước 6: Lấy thông tin Phòng ban mới
         * --------------------------------------------------------
         */
        Departments departments =
                departmentService.findEntityById(request.getDepartmentId()
                );

        /*
         * --------------------------------------------------------
         * Bước 7: Tiến hành đè dữ liệu mới lên thực thể cũ (Update Entity)
         * --------------------------------------------------------
         */
        // Nhờ mapper tự động copy các thông tin cơ bản từ request đè lên đối tượng employee cũ
        employeeMapper.updateEntity(request, employee);

        //Gán lại các mối quan hệ phòng ban, tài khoản và mã nhân viên sạch sau khi kiểm tra thành công
        employee.setUser(user);
        employee.setDepartment(departments);
        employee.setEmployeeCode(employeeCode);

        /*
         * --------------------------------------------------------
         * Bước 8: Lưu thông tin Nhân viên mới cập nhật xuống Database
         * --------------------------------------------------------
         */
        Employee updatedEmployee =
                employeeRepository.save(employee);

        /*
         * --------------------------------------------------------
         * Bước 9: Chuyển đổi dữ liệu sạch (Entity → Response DTO) để trả về kết quả
         * --------------------------------------------------------
         */
        return employeeMapper.toResponse(updatedEmployee);
    }

    /**
     * ============================================================
     * 5. XÓA MỀM NHÂN VIÊN (SOFT DELETE)
     * ============================================================
     */
    @Override
    public void delete(Long id) {

        //Vào DB tìm kiếm nhân viên cần xóa. Nếu k thấy hoặc đã xóa -> Báo lỗi chặn lại
        Employee employee = employeeRepository
                .findById(id)
                .filter(existing -> !existing.isDeleted())
                .orElseThrow(() ->
                            new BusinessException("error.employee.notFound")
                        );

        /*
         * Tuyệt đối KHÔNG dùng hàm cứng: repository.delete().
         * Vì Nhân viên kế thừa từ lớp BaseEntity có tính năng Xóa mềm (Soft Delete).
         * Ta chỉ cần chuyển cờ trạng thái 'deleted' từ false thành true để ẩn nhân viên này đi.
         */
        employee.setDeleted(true);

        //Lưu lại trạng thái ẩn này xuống Database
        employeeRepository.save(employee);
    }

    /**
     * ============================================================
     * PRIVATE METHODS (CÁC HÀM BỔ TRỢ NỘI BỘ PHỤC VỤ PHÂN TRANG)
     * ============================================================
     */

    /**
     * Tạo Pageable từ EmployeeSearchRequest.
     *  Tạo khuôn cấu hình phân trang Pageable từ gói yêu cầu EmployeeSearchRequest.
     */
    private Pageable createPageable(EmployeeSearchRequest request) {

        int page = request.getPage(); //Lấy số thứ tự trang muốn xem
        int size = request.getSize(); //Lấy số lượng dòng muốn xem trên 1 trang

        //Nhờ hàm helper dọn dẹp và lấy ra tên cột dữ liệu hợp lệ để sắp xếp
        String sortProperty =
                resolveSortProperty(request.getSortBy());

        //Nhờ hàm helper xác định chiều sắp xếp là Tăng dần (ASC) hay Giảm dần (DESC)
        Sort.Direction direction =
                resolveSortDirection(request.getSortDirection());

        // Gộp chiều và tên cột lại thành một đối tượng sắp xếp hoàn chỉnh
        Sort sort = Sort.by(direction, sortProperty);

        // Trả về khuôn phân trang chuẩn chỉnh của Spring Data
        return PageRequest.of(page, size, sort);
    }
    /**
     * Danh sách Whitelist (Bộ lọc an toàn): Chỉ cho phép sắp xếp theo một số trường định sẵn.
     * Ngăn chặn việc client truyền bậy tên trường không tồn tại làm lỗi câu lệnh SQL.
     */
    private String resolveSortProperty(String sortBy) {

        // Nếu người dùng để trống k chọn cột sắp xếp nào
        if (!StringUtils.hasText(sortBy)) {
            return DEFAULT_SORT_PROPERTY; // Trả về cột mặc định là "id"
        }

        //Sử dụng Switch cú pháp mới của Java:
        //Nếu trường truyền lên khớp với một trong các hằng số an toàn (id, mã NV, tên, email, ngày vào làm)
        return switch (sortBy) {
            case SORT_ID,
                 SORT_EMPLOYEE_CODE,
                 SORT_FULL_NAME,
                 SORT_EMAIL,
                 SORT_JOIN_DATE -> sortBy; // Trả về chính cột đó để đi sắp xếp

            default -> DEFAULT_SORT_PROPERTY; // Nếu gõ linh tinh, trừng phạt bằng cách ép về sắp xếp theo cột "id"
        };
    }

    /**
     * Xử lý chiều sort.(Xử lý xác định chiều sắp xếp (Tăng dần - ASC / Giảm dần - DESC).)
     */
    private Sort.Direction resolveSortDirection(
            String sortDirection
    ) {

        //Nếu người dùng không chọn chiều sắp xếp nào -> Mặc định chọn tăng dần (ASC)
        if (!StringUtils.hasText(sortDirection)) {
            return Sort.Direction.ASC;
        }

        // So sánh chuỗi không phân biệt chữ hoa chữ thường: Nếu gõ chữ "DESC" hoặc "desc"
        // -> Trả về Giảm dần (DESC), ngược lại tất cả các trường hợp khác đều coi là Tăng dần (ASC)
        return "DESC".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
    }
}
