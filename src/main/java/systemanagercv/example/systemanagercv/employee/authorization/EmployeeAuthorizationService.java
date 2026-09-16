package systemanagercv.example.systemanagercv.employee.authorization;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import systemanagercv.example.systemanagercv.common.enums.RoleName;
import systemanagercv.example.systemanagercv.employee.entity.Employee;
import systemanagercv.example.systemanagercv.employee.repository.EmployeeRepository;
import systemanagercv.example.systemanagercv.user.entity.User;
import systemanagercv.example.systemanagercv.user.service.UserService;

@Service // Đánh dấu tầng Service hỗ trợ kiểm tra an ninh hệ thống
@RequiredArgsConstructor // tự động tạo contrustor và ql các final
public class EmployeeAuthorizationService {

    private final UserService userService;
    private final EmployeeRepository employeeRepository;

    /**
     * CHỨC NĂNG CHÍNH 1: Kiểm tra tổng quát xem tài khoản hiện tại có quyền XEM hồ sơ nhân viên mục tiêu hay không.
     * Quy tắc cấu hình:
     * - ADMIN / HR  : Được xem TUYỆT ĐỐI tất cả nhân viên trong công ty.
     * - TECH_LEAD   : Chỉ được phép xem nhân viên CÙNG PHÒNG BAN với mình.
     * - EMPLOYEE    : Chỉ được phép xem DUY NHẤT hồ sơ của chính bản thân mình.
     */

    public boolean canView(Long employeeId) {

        // Bước 1: Lấy thông tin tài khoản của người đang bấm nút xem trên giao diện (getCurrentUser viết ở dưới)
        User currentUser = getCurrentUser();

        // Bước 2: Đặc quyền tối cao - Nếu là ADMIN hoặc HR thì lập tức cho qua cửa (return true) luôn, không cần xét tiếp
        if (hasRole(currentUser, RoleName.ADMIN)) {
            return true;
        }

        if (hasRole(currentUser, RoleName.HR)) {
            return true;
        }

        // Bước 3: Tìm thông tin hồ sơ nhân viên mục tiêu mà tài khoản này đang muốn click vào xem theo ID
        Employee targetEmployee = employeeRepository
                .findById(employeeId)
                .orElse(null);

        // Chốt chặn an toàn: Nếu hồ sơ này k có thật hoặc đã bị đánh dấu xóa mềm -> Trả về lỗi không cho xem (false)
        if (targetEmployee == null || targetEmployee.isDeleted()) {
            return false;
        }

        // Bước 4: Xét phân quyền cho nhóm nhân viên thông thường (EMPLOYEE)
        if (hasRole(currentUser, RoleName.EMPLOYEE)) {
            // Chỉ cho xem nếu hồ sơ mục tiêu trùng với chính bản thân họ (isOwnEmployee viết ở dưới)
            return isOwnEmployee(currentUser, targetEmployee);
        }

        // Bước 5: Xét phân quyền cho nhóm Trưởng nhóm kỹ thuật (TECH_LEAD)
        if (hasRole(currentUser, RoleName.TECH_LEAD)) {
            // Chỉ cho xem nếu nhân viên mục tiêu thuộc cùng một phòng ban với họ (isSameDepartment viết ở dưới)
            return isSameDepartment(currentUser, targetEmployee);
        }
        // Tất cả các trường hợp lạ khác (ví dụ tài khoản không có quyền gì) -> Đều bị cấm cửa (false)
        return false;
    }

    /**
     * HÀM KIỂM TRA QUYỀN TẠO MỚI (DỰA TRÊN ID PHÒNG BAN MUỐN THÊM)
     * @param deparmentId: ID phòng ban mà người dùng đang muốn thêm dữ liệu vào
     * @return true nếu được phép tạo, false nếu bị cấm
     */
    public boolean canCreate(Long deparmentId){

        // BƯỚC 1: Lấy thông tin đối tượng User của người đang đăng nhập hiện tại
        User currentUser = getCurrentUser();

        // Chốt chặn 1: Nếu tài khoản là ADMIN tối cao -> Cho phép tạo thoải mái ở bất kỳ phòng ban nào
        if (hasRole(currentUser, RoleName.ADMIN)) {
            return true;
        }

        // Chốt chặn 2: Nếu tài khoản là nhân sự HR -> Cũng được phép tạo thoải mái (vì HR quản lý toàn công ty)
        if (hasRole(currentUser, RoleName.HR)) {
            return true;
        }

        // Chốt chặn 3: PHÂN QUYỀN HẠN HẸP CHO TRƯỞNG NHÓM (TECH_LEAD)
        if (hasRole(currentUser, RoleName.TECH_LEAD)) {

            // Đi sâu vào lấy thông tin hồ sơ Nhân viên liên kết với tài khoản này
            Employee currentEmployee =
                    currentUser.getEmployee();

            // Phòng thủ an toàn: Nếu tài khoản TECH_LEAD này chưa được tạo hồ sơ nhân viên,
            // hoặc hồ sơ nhân viên chưa được gán vào phòng ban nào -> Chặn lại, cấm tạo (return false)
            if (currentEmployee == null
                || currentEmployee.getDepartment() == null) {
                return false;
            }

            /*
             * LUẬT CHO TECH_LEAD: Trưởng phòng nào chỉ được tạo dữ liệu cho phòng đó!
             * Lấy ID phòng ban của chính ông TECH_LEAD này so sánh với ID phòng ban mà ông ấy đang muốn thêm mới dữ liệu.
             * - Nếu TRÙNG KHỚP -> Trả về true (Được phép tạo).
             * - Nếu LỆCH NHAU (ông IT muốn tạo dữ liệu cho phòng Kế toán) -> Trả về false (Cấm tuyệt đối).
             */
            return currentEmployee
                    .getDepartment()
                    .getId()
                    .equals(deparmentId);
        }
        // 👤 CHỐT CHẶN CUỐI CÙNG: Nếu là Nhân viên thường (EMPLOYEE) hoặc người lạ -> Cấm tuyệt đối không được tạo gì hết!
        return false;
    }

    /**
     * HÀM KIỂM TRA QUYỀN CHỈNH SỬA (UPDATE) HỒ SƠ NHÂN VIÊN
     * @param employee: Đối tượng nhân viên cũ đang chuẩn bị được sửa thông tin
     * @param targetDepartmentId: ID phòng ban mới mà người dùng muốn gán cho nhân viên này (nếu có đổi phòng)
     * @return true nếu có quyền sửa, false nếu bị chặn
     */
    public boolean canUpdate(
            Employee employee,
            Long targetDepartmentId
    ){
        // Bước 1: Lấy thông tin tài khoản của người đang thực hiện hành động bấm nút sửa
        User currentUser = getCurrentUser();

        // CHỐT CHẶN 1: Nếu là ADMIN tối cao -> cho phép chỉnh sửa bất kỳ ai, chuyển đi đâu cũng được
        if (hasRole(currentUser, RoleName.ADMIN)) {
            return true;
        }

        // CHỐT CHẶN 2: Nếu là nhân sự HR -> Cũng được toàn quyền chỉnh sửa (vì HR quản lý nhân sự toàn công ty)
        if (hasRole(currentUser, RoleName.HR)) {
            return true;
        }

        // CHỐT CHẶN 3: PHÂN QUYỀN CHO TRƯỞNG NHÓM (TECH_LEAD)
        if (hasRole(currentUser, RoleName.TECH_LEAD)) {

            // Lấy thông tin hồ sơ nhân viên của chính ông TECH_LEAD này
            Employee currentEmployee = currentUser.getEmployee();

            // Phòng thủ an toàn: Nếu hồ sơ của ông Tech lead, hoặc hồ sơ nhân viên bị sửa đang trống dữ liệu phòng ban -> Chặn lại
            if (currentEmployee == null
                    || currentEmployee.getDepartment() == null
                    || employee.getDepartment() == null){
                return false;
            }

            // Đọc ra ID phòng ban của ông TECH_LEAD đang đăng nhập
            Long currentDepartmentId = currentEmployee.getDepartment().getId();

            // Đọc ra ID phòng ban hiện tại của nhân viên đang chuẩn bị , bị sửa
            Long employeeDepartmentId = employee.getDepartment().getId();

            /*
             * LUẬT CHO TECH_LEAD (Bắt buộc thỏa mãn cả 2 điều kiện):
             * 1. Nhân viên bị sửa phải đang thuộc phòng của ông Tech Lead này quản lý (currentDepartmentId == employeeDepartmentId)
             * 2. Phòng ban mới gán cho nhân viên đó cũng phải là phòng của ông Tech Lead (currentDepartmentId == targetDepartmentId)
             * 👉 Ý nghĩa: Trưởng phòng IT chỉ được sửa nhân viên phòng IT và CẤM không được tự ý chuyển nhân viên đó sang phòng Kế toán!
             */
            return currentDepartmentId.equals(employeeDepartmentId)
                    && currentDepartmentId.equals(targetDepartmentId);
        }

        // CHỐT CHẶN 4: PHÂN QUYỀN CHO NHÂN VIÊN THƯỜNG (EMPLOYEE)
        if (hasRole(currentUser, RoleName.EMPLOYEE)) {

            // Lấy thông tin hồ sơ nhân viên của chính tài khoản EMPLOYEE này
            Employee currentEmployee = currentUser.getEmployee();

            // Nếu nhân viên thường này chưa được tạo hồ sơ -> Chặn
            if (currentEmployee == null){
                return false;
            }

            /*
             * LUẬT CHO EMPLOYEE (Bắt buộc thỏa mãn cả 2 điều kiện):
             * 1. Người đăng nhập chỉnh sửa phải chính là chủ nhân của hồ sơ đó (currentEmployee.getId() == employee.getId())
             * 2. Khi tự sửa hồ sơ của mình, CẤM tuyệt đối không được tự ý đổi phòng ban sang phòng khác (Phải giữ nguyên phòng cũ).
             */
            return currentEmployee.getId().equals(employee.getId())
                    && currentEmployee.getDepartment() != null
                    && currentEmployee.getDepartment().getId()
                    .equals(targetDepartmentId);
        }

        // ❌ CHỐT CHẶN CUỐI: Người lạ không có quyền, cấm sửa!
        return false;

    }

    /**
     * CHỨC NĂNG: Kiểm tra xem User đang đăng nhập có quyền XÓA hồ sơ nhân viên mục tiêu hay không.
     * Quy tắc phân quyền:
     *   - ADMIN / HR: Được quyền xóa bất kỳ ai trên hệ thống.
     *   - TECH_LEAD : Chỉ được quyền xóa nhân viên thuộc CÙNG PHÒNG BAN với mình.
     *   - Các quyền khác: Bị từ chối, không được xóa ai.
     */
    public boolean canDelete(Employee employee){
        if (employee == null){
            return false;
        }
        // Bước 1: Thò tay vào túi áo hệ thống bốc ra thông tin tài khoản của người đang thực hiện lệnh xóa
        User currentUser = getCurrentUser();

        // Bước 2: Sếp lớn (ADMIN) -> cho phép xóa ngay lập tức (return true)
        if (hasRole(currentUser, RoleName.ADMIN)) {
            return true;
        }

        // Bước 3: Phòng nhân sự (HR) -> Cho phép xóa ngay lập tức
        if (hasRole(currentUser, RoleName.HR)) {
            return true;
        }

        // Bước 4: Xét quyền cho Trưởng nhóm kỹ thuật (TECH_LEAD)
        if (hasRole(currentUser, RoleName.TECH_LEAD)) {
            // Lấy thông tin hồ sơ nhân viên gắn liền với tài khoản TECH_LEAD đang đăng nhập này
            Employee currentEmployee = currentUser.getEmployee();

            // Chốt chặn phòng ngừa lỗi NullPointerException:
            // Nếu bản thân ông TECH_LEAD này chưa có hồ sơ, hoặc tài khoản của ông ấy chưa được gán phòng ban,
            // hoặc người nhân viên bị xóa chưa được gán phòng ban nào -> Báo lỗi từ chối không cho xóa (return false)
            if (currentEmployee == null
                || currentEmployee.getDepartment() == null
                || employee.getDepartment() == null){
                return false;
            }

            // Bốc ra ID phòng ban của ông TECH_LEAD đang đăng nhập
            Long currentDepartmentId = currentEmployee.getDepartment().getId();

            // Bốc ra ID phòng ban của người nhân viên sắp sửa bị xóa
            Long employeeDepartmentId = employee.getDepartment().getId();

            // Tiến hành so sánh: Nếu hai ID phòng ban trùng khớp 100% với nhau
            // -> Trả về true (Đồng ý cho xóa nhân viên cùng bộ phận), ngược lại trả về false (Cấm xóa người phòng khác)
            return currentDepartmentId.equals(employeeDepartmentId);
        }

        // Bước 5: Nếu rơi vào các quyền thấp hơn (như EMPLOYEE thường) -> Mặc định cấm cửa hoàn toàn không cho xóa
        return false;
    }

    /**
     * CHỨC NĂNG CHÍNH 2: Chốt chặn ép buộc áp dụng riêng - Chỉ cho phép xem nếu đó là CHÍNH MÌNH.
     */
    public  boolean canViewOwn(Long employeeId) {

        User currentUser = getCurrentUser();

        Employee targetEmployee = employeeRepository
                .findById(employeeId)
                .orElse(null);

        if (targetEmployee == null || targetEmployee.isDeleted()) {
            return false;
        }

        // Bắt buộc so sánh chính chủ
        return isOwnEmployee(currentUser, targetEmployee);
    }

    /**
     * CHỨC NĂNG CHÍNH 3: Chốt chặn ép buộc áp dụng riêng - Chỉ cho phép xem nếu CÙNG PHÒNG BAN.
     */
    public boolean canViewSameDepartment(Long employeeId) {

        User currentUser = getCurrentUser();
        Employee targetEmployee = employeeRepository
                .findById(employeeId)
                .orElse(null);

        if (targetEmployee == null || targetEmployee.isDeleted()) {
            return false;
        }
        // Bắt buộc so sánh trùng phòng ban
        return isSameDepartment(currentUser, targetEmployee);
    }

    /**
     * HÀM TRỢ GIÚP 1: Kiểm tra xem hồ sơ nhân viên mục tiêu có phải là của chính tài khoản đang đăng nhập hay không.
     */
    private boolean isOwnEmployee(
            User currentUser,
            Employee targetEmployee
    ){
        // Nếu tài khoản User hiện tại thậm chí còn chưa được liên kết với một hồ sơ nhân viên nào -> Trả về false
        if (currentUser.getEmployee() == null){
            return false;
        }

        // So sánh mã ID hồ sơ nhân viên của tài khoản đang đang đăng nhập xem có bằng với ID hồ sơ mục tiêu hay không
        return currentUser.getEmployee().getId().equals(targetEmployee.getId());
    }

    /**
     * HÀM TRỢ GIÚP 2: Kiểm tra xem hai người có đang làm việc chung một Phòng ban (Department) hay không.
     */
    private boolean isSameDepartment(
            User currentUser,
            Employee targetEmployee
    ){
        // Tài khoản đang đăng nhập phải có hồ sơ nhân viên gắn kèm
        if (currentUser.getEmployee() == null){
            return false;
        }

        Employee currentEmployee = currentUser.getEmployee();

        // Đảm bảo cả người đang xem và người bị xem đều phải đang thuộc về một phòng ban nào đó (tránh lỗi null)
        if (currentEmployee.getDepartment() == null){
            return false;
        }

        if (targetEmployee.getDepartment() == null) {
            return false;
        }

        // Lấy mã ID phòng ban của hai bên đem đi so sánh xem có trùng nhau 100% hay không
        return currentEmployee.getDepartment()
                .getId()
                .equals(
                        targetEmployee.getDepartment().getId()
                );

    }

    /**
     * HÀM TRỢ GIÚP 3: Thò tay vào chiếc túi áo hệ thống (SecurityContext) bốc ra thông tin tài khoản đang đăng nhập hiện tại
     */
    private User getCurrentUser(){
        // Lấy chứng nhận đăng nhập đã được đóng dấu từ người gác cổng bộ lọc JWT ở bài học trước
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        // Chốt chặn: Nếu túi áo rỗng hoặc chứng nhận chưa được xác thực thành công -> Báo lỗi hệ thống chặn lại lập tức
        if (authentication == null
            || !authentication.isAuthenticated()){

            throw new IllegalStateException("Current user is not authenticated");
        }

        // Bốc lấy cái Username ghi trên vé, nhờ userService chạy xuống database tìm toàn bộ thông tin User Entity lên trả về
        return userService.findByUsername(authentication.getName());
    }
    // Kiểm tra phạm vị truy cập của User hiện tại
    public EmployeeAccessScope getCurrentUserScope() {

        User currentUser = getCurrentUser();

        if (hasRole(currentUser, RoleName.ADMIN)
                || hasRole(currentUser, RoleName.HR)) {

            return EmployeeAccessScope.builder()
                    .accessType(EmployeeAccessType.ALL)
                    .build();
        }

        if (hasRole(currentUser, RoleName.TECH_LEAD)) {

            Employee currentEmployee =
                    currentUser.getEmployee();

            if (currentEmployee == null
                    || currentEmployee.getDepartment() == null) {

                return EmployeeAccessScope.builder()
                        .accessType(EmployeeAccessType.DEPARTMENT)
                        .departmentId(null)
                        .build();
            }

            return EmployeeAccessScope.builder()
                    .accessType(EmployeeAccessType.DEPARTMENT)
                    .departmentId(
                            currentEmployee
                                    .getDepartment()
                                    .getId()
                    )
                    .build();
        }

        if (hasRole(currentUser, RoleName.EMPLOYEE)) {

            Employee currentEmployee =
                    currentUser.getEmployee();

            if (currentEmployee == null) {

                return EmployeeAccessScope.builder()
                        .accessType(EmployeeAccessType.SELF)
                        .employeeId(null)
                        .build();
            }

            return EmployeeAccessScope.builder()
                    .accessType(EmployeeAccessType.SELF)
                    .employeeId(
                            currentEmployee.getId()
                    )
                    .build();
        }

        return EmployeeAccessScope.builder()
                .accessType(EmployeeAccessType.SELF)
                .employeeId(null)
                .build();
    }

    /**
     * HÀM TRỢ GIÚP 4: Kiểm tra xem tài khoản User đó có sở hữu tên quyền truyền vào hay không.
     */
    /**
     * Kiểm tra role của User.
     *
     * DB:
     * ADMIN
     * HR
     * TECH_LEAD
     * EMPLOYEE
     */
    private boolean hasRole(
            User user,
            RoleName roleName
    ){
        // Bật băng chuyền Stream lặp mảng danh sách quyền của User hiện tại để đối chiếu chuỗi chữ cái
        return user.getUserRoles()
                .stream()
                .anyMatch(userRole ->
                        userRole.getRole()
                                .getName() // Lấy đối tượng Enum quyền
                                .equals(roleName.name()) // So sánh xem có khớp với quyền đang cần xét hay không
                );
    }
}
