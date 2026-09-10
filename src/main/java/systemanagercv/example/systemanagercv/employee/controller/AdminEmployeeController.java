package systemanagercv.example.systemanagercv.employee.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import systemanagercv.example.systemanagercv.department.service.DepartmentService;
import systemanagercv.example.systemanagercv.employee.dto.request.EmployeeCreateRequest;
import systemanagercv.example.systemanagercv.employee.dto.request.EmployeeSearchRequest;
import systemanagercv.example.systemanagercv.employee.dto.request.EmployeeUpdateRequest;
import systemanagercv.example.systemanagercv.employee.dto.response.EmployeeDetailResponse;
import systemanagercv.example.systemanagercv.employee.dto.response.EmployeeResponse;
import systemanagercv.example.systemanagercv.employee.enums.EmployeePosition;
import systemanagercv.example.systemanagercv.employee.enums.EmployeeStatus;
import systemanagercv.example.systemanagercv.employee.service.EmployeeService;
import systemanagercv.example.systemanagercv.user.service.UserService;

@Controller
@RequestMapping("/admin/employees")
@RequiredArgsConstructor
//AdminEmployeeController → Thymeleaf /admin/employees
public class AdminEmployeeController {

    private final EmployeeService employeeService;
    private final UserService userService;
    private final DepartmentService departmentService;

    // =========================================================
    // 1. TRANG DANH SÁCH EMPLOYEE
    //
    // GET /admin/employees
    //
    // Có:
    // - Tìm kiếm
    // - Phân trang
    // - Sắp xếp
    // =========================================================
    @GetMapping
    public String index(
            @Valid @ModelAttribute EmployeeSearchRequest request,
            BindingResult bindingResult, // Sổ ghi lỗi kiểm tra dữ liệu request đầu vào
            Model model // Chuyển dữ liệu từ code Java cho file Html giao diện
    ) {

        //Nếu thông số phân trang hoặc bộ lọc người dùng nhập vào bị lỗi quy định
        if (bindingResult.hasErrors()) {
            request = new EmployeeSearchRequest(); // Khởi tạo lại request trống mặc định để cứu an toàn cho app
        }

        // Gọi Service thực hiện quét tìm kiếm nâng cao + phân trang nhân viên từ DB lên
        Page<EmployeeResponse> employeePage =
                employeeService.search(request);

        System.out.println("========== EMPLOYEE DEBUG ==========");
        System.out.println("Total employees: " + employeePage.getTotalElements());
        System.out.println("Content size: " + employeePage.getContent().size());
        System.out.println("Employees: " + employeePage.getContent());
        System.out.println("====================================");


        // Đẩy danh sách nhân viên của trang hiện tại sang cho View HTML lặp mảng vẽ lên bảng
        model.addAttribute("employees", employeePage.getContent());

        // Giữ lại các giá trị lọc cũ mà người dùng vừa chọn để khi tải lại trang, các ô tìm kiếm không bị xóa trắng
        model.addAttribute("keyword", request.getKeyword());
        model.addAttribute("departmentId", request.getDepartmentId());
        model.addAttribute("position", request.getPosition());
        model.addAttribute("status", request.getStatus());

        // Đẩy thông số kích thước số dòng hiển thị và kiểu sắp xếp hiện tại sang cho View
        model.addAttribute("pageSize", request.getSize());
        model.addAttribute("sortBy", request.getSortBy());
        model.addAttribute("sortDirection", request.getSortDirection());

        //Spring tính trang từ 0, giao diện hiển thị tính từ số 1 nên phải cộng thêm 1
        model.addAttribute("currentPage", employeePage.getNumber() + 1);

        model.addAttribute(
                "totalPages",
                employeePage.getTotalPages() // Tổng số lượng trang có thể chia được
        );

        model.addAttribute(
                "totalItems",
                employeePage.getTotalElements() // Tổng số lượng nhân viên thỏa mãn điều kiện lọc trên toàn hệ thống
        );

        // ========================================================
        // 5. Enum cho View
        // ========================================================
        // Lấy toàn bộ danh sách các chức vụ định sẵn từ file cấu hình Enum gán vào giỏ để vẽ ô chọn Dropdown
        model.addAttribute(
                "positions",
                EmployeePosition.values()
        );

        // Lấy toàn bộ danh sách các trạng thái làm việc định sẵn từ file cấu hình Enum gán vào giỏ
        model.addAttribute(
                "statuses",
                EmployeeStatus.values()
        );

        // Lấy tất cả các phòng ban trong hệ thống để làm dữ liệu lọc cho ô chọn phòng ban
        model.addAttribute(
                "departments",
                departmentService.getActiveDepartments()
        );

        return "admin/employee/index";
    }

    // =========================================================
    // 2. MÀN HÌNH HIỂN THỊ BIỂU MẪU (FORM) THÊM MỚI NHÂN VIÊN
    // Đường dẫn: GET /admin/employees/add
    // =========================================================
    @GetMapping("/add")
    public String add(Model model) {

        //Khởi tạo một đối tượng form trống để làm khuôn liên kết (Binding) dữ liệu với các ô nhập liệu Form trên HTML
        EmployeeCreateRequest request =
                new EmployeeCreateRequest();

        model.addAttribute("employee", request);

        // ========================================================
        // 2. USER ACCOUNT
        //
        // QUAN TRỌNG:
        //
        // Danh sách này hiện bao gồm:
        //
        // HR
        // TECH_LEAD
        // EMPLOYEE
        //
        // Không bao gồm ADMIN.
        // ========================================================
        // Lấy danh sách các tài khoản User sạch (Có quyền ROLE_EMPLOYEE) gửi sang để hiển thị ô chọn liên kết tài khoản
        model.addAttribute("users", userService.getEmployeeUsers());

        // Lấy danh sách tất cả các phòng ban gửi sang để gán phòng ban làm việc cho nhân viên mới
        model.addAttribute(
                "departments",
                departmentService.getActiveDepartments()
        );

        // Nạp mảng hằng số chức vụ Enum (Ví dụ: DEVELOPER, TESTER...) cho ô chọn chức vụ
        model.addAttribute(
                "positions",
                EmployeePosition.values()
        );

        // Nạp mảng hằng số trạng thái Enum (Ví dụ: ACTIVE, LEAVE...) cho ô chọn trạng thái
        model.addAttribute(
                "statuses",
                EmployeeStatus.values()
        );
        return "admin/employee/add";
    }

    // =========================================================
    // 3. XỬ LÝ KHI NGƯỜI DÙNG BẤM NÚT "LƯU" THÊM MỚI NHÂN VIÊN
    // Đường dẫn: POST /admin/employees/add
    // =========================================================
    @PostMapping("/add")
    public String create(
            @Valid @ModelAttribute("employee") EmployeeCreateRequest request, // Đón nhận gói dữ liệu chứa thông tin nhân viên vừa điền từ Form gửi lên
            BindingResult bindingResult, // Sổ kiểm tra lỗi nhập liệu (Ví dụ: Mã NV bỏ trống, sai cấu trúc)
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        // Nếu người dùng điền thiếu thông tin bắt buộc hoặc nhập sai định dạng quy định của Form
        if (bindingResult.hasErrors()) {

            // Nạp lại dữ liệu cho ô chọn User (Vì khi trang tải lại báo lỗi, giỏ Model cũ sẽ bị xóa mất dữ liệu)
            model.addAttribute(
                    "users",
                    userService.getEmployeeUsers()
            );
            // Nạp lại dữ liệu cho ô chọn Phòng ban
            model.addAttribute(
                    "departments",
                    departmentService.getActiveDepartments()
            ); // Đã sửa dấu hai chấm thành dấu chấm phẩy chuẩn cú pháp Java ở đây

            // Tiếp tục bổ sung nạp lại các ô chọn còn thiếu để giao diện không bị mất danh sách chọn khi báo lỗi
            model.addAttribute(
                    "positions",
                    EmployeePosition.values()
            );
            model.addAttribute(
                    "statuses",
                    EmployeeStatus.values()
            );

            // Giữ người dùng ở lại trang điền đơn để họ nhìn thấy các dòng chữ báo lỗi màu đỏ và sửa lại tại chỗ
            return "admin/employee/add";
        }

        // CREATE EMPLOYEE
        employeeService.create(request);

        // ========================================================
        // 3. Success message
        // ========================================================
        redirectAttributes.addFlashAttribute(
                "message",
                "Employee created successfully!"
        );

        // [Mã nguồn của bạn đang tạm dừng tại đây]
        // Đoạn code xử lý tiếp theo chuẩn chỉnh sẽ là: Gọi Service lưu dữ liệu và chuyển hướng quay về trang danh sách chính.
        return "redirect:/admin/employees";
    }

    // =========================================================
    // 4. MÀN HÌNH CHỈNH SỬA EMPLOYEE
    // Đường dẫn: GET /admin/employees/edit/{id} (Ví dụ: /admin/employees/edit/5)
    // =========================================================
    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model
    ) {

        // =====================================================
        // 1. LẤY THÔNG TIN EMPLOYEE
        // =====================================================

        EmployeeDetailResponse employee =
                employeeService.findById(id);


        // =====================================================
        // 2. KIỂM TRA EMPLOYEE
        // =====================================================

        if (employee == null) {
            return "redirect:/admin/employees";
        }


        // =====================================================
        // 3. CHUYỂN EmployeeDetailResponse
        //    → EmployeeUpdateRequest
        //  Map Response → Update Request
        // =====================================================

        EmployeeUpdateRequest request =
                new EmployeeUpdateRequest();

        request.setUserId(employee.getUserId());
        request.setEmployeeCode(employee.getEmployeeCode());
        request.setFullName(employee.getFullName());
        request.setEmail(employee.getEmail());
        request.setPhone(employee.getPhone());
        request.setDepartmentId(employee.getDepartmentId());
        request.setPosition(employee.getPosition());
        request.setJobTitle(employee.getJobTitle());
        request.setJoinDate(employee.getJoinDate());
        request.setStatus(employee.getStatus());


        // =====================================================
        // 4. ĐƯA EMPLOYEE VÀO FORM
        // =====================================================

        model.addAttribute(
                "employee",
                request
        );


        // =====================================================
        // 5. ID EMPLOYEE ĐANG EDIT
        // =====================================================

        model.addAttribute(
                "employeeId",
                id
        );


        // =====================================================
        // 6. LẤY USER CHO FORM EDIT
        //
        //    QUAN TRỌNG:
        //    KHÔNG dùng getEmployeeUsers()
        //
        //    Phải dùng:
        //    getEmployeeUsersForEdit(id)
        // =====================================================

        model.addAttribute(
                "users",
                userService.getEmployeeUsersForEdit(id)
        );


        // =====================================================
        // 7. PHÒNG BAN
        // =====================================================

        model.addAttribute(
                "departments",
                departmentService.getActiveDepartments()
        );


        // =====================================================
        // 8. CHỨC VỤ
        // =====================================================

        model.addAttribute(
                "positions",
                EmployeePosition.values()
        );


        // =====================================================
        // 9. TRẠNG THÁI
        // =====================================================

        model.addAttribute(
                "statuses",
                EmployeeStatus.values()
        );


        // =====================================================
        // 10. VIEW
        // =====================================================

        return "admin/employee/edit";
    }

    // =========================================================
    // 5. XỬ LÝ CẬP NHẬT EMPLOYEE
    // Đường dẫn: POST /admin/employees/edit/{id} (Ví dụ: /admin/employees/edit/5)
    // =========================================================
    @PostMapping("/edit/{id}")
    public String update(
            @PathVariable Long id, // Lấy ID của nhân viên từ URL
            @Valid @ModelAttribute("employee") EmployeeUpdateRequest request,
            BindingResult bindingResult, //Cuốn sổ ghi lỗi kiểm tra Form
            Model model,
            RedirectAttributes redirectAttributes // Công cụ tạo dòng thông báo nhanh hiện lên một lần rồi tự biến mất (Alert)
    ){
        /*
         * --------------------------------------------------------
         * Bước 1: Kiểm tra dữ liệu nhập trên Form (Validation)
         * --------------------------------------------------------
         * @Valid sẽ kiểm tra tự động xem các trường như:
         * userId, employeeCode, fullName, email, phone, departmentId, status...
         * có vi phạm các điều kiện (như @NotBlank, @Email, @Size...) quy định trong class DTO hay không.
         */
        if (bindingResult.hasErrors()) { // Nếu cuốn sổ ghi lỗi phát hiện có bất kỳ lỗi nhập liệu nào từ Form

            /*
             * Khi dữ liệu Form bị lỗi, trình duyệt sẽ tải lại trang sửa này.
             * Để các ô chọn (Dropdown) không bị trống rỗng dữ liệu, Controller bắt buộc phải
             * nạp lại (re-populate) toàn bộ danh sách lựa chọn vào giỏ Model tương tự như hàm showEditForm.
             */

            // Giữ lại Employee ID để Form Thymeleaf trên giao diện biết đang thực hiện sửa cho nhân viên nào
            model.addAttribute(
                    "employeeId",
                    id
            );

            // ====================================================
            // QUAN TRỌNG:
            //
            // Không dùng getEmployeeUsers()
            //
            // Phải dùng getEmployeeUsersForEdit(id)
            //
            // để User hiện tại vẫn xuất hiện.
            // ====================================================
            // Nạp lại danh sách tài khoản User có quyền ROLE_EMPLOYEE cho ô chọn Tài khoản
            model.addAttribute(
                    "users",
                    userService.getEmployeeUsersForEdit(id)
            );

            // Nạp lại danh sách các phòng ban đang mở (ACTIVE) cho ô chọn Phòng ban
            model.addAttribute(
                    "departments",
                    departmentService.getActiveDepartments()
            );

            // Nạp lại mảng hằng số chức vụ Enum cho ô chọn Chức vụ
            model.addAttribute(
                    "positions",
                    EmployeePosition.values()
            );

            // Nạp lại mảng hằng số trạng thái Enum cho ô chọn Trạng thái
            model.addAttribute(
                    "statuses",
                    EmployeeStatus.values()
            );

            /*
             * Quay lại chính file giao diện sửa: src/main/resources/templates/admin/employee/edit.html
             * Lúc này người dùng sẽ nhìn thấy các dữ liệu họ vừa nhập sai kèm theo dòng chữ báo lỗi màu đỏ.
             */
            return "admin/employee/edit";
        }

        /*
         * --------------------------------------------------------
         * Bước 2: Dữ liệu Form hợp lệ -> Gọi tầng Service thực hiện cập nhật
         * --------------------------------------------------------
         * Hàm employeeService.update(id, request) (bạn đã học ở file Service trước) sẽ chạy chuỗi 9 bước:
         * - Kiểm tra nhân viên có tồn tại hoặc bị xóa mềm không
         * - Kiểm tra Mã nhân viên mới có trùng với ai khác không (loại trừ chính mình)
         * - Kiểm tra tài khoản User mới có hợp lệ và đúng quyền EMPLOYEE không
         * - Kiểm tra tài khoản User đó đã bị nhân viên khác chiếm dụng chưa
         * - Kiểm tra Phòng ban mới chọn có tồn tại không
         * - Đè dữ liệu mới vào Entity và thực hiện Lưu xuống Database dưới cờ bảo hiểm @Transactional
         */
        employeeService.update(id, request);

        /*
         * --------------------------------------------------------
         * Bước 3: Đính kèm thông báo thành công dạng Flash Attribute
         * --------------------------------------------------------
         * Thông báo này sẽ được đính kèm vào đợt chuyển hướng trang, giúp hiển thị một thanh thông báo xanh
         * sạch đẹp (Alert Success) ở trang tiếp theo rồi tự biến mất khi bấm F5 tải lại trang.
         */
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Cập nhật nhân viên thành công."
        );

        /*
         * --------------------------------------------------------
         * Bước 4: Ra lệnh cho trình duyệt tự chuyển hướng quay về trang danh sách chính
         * --------------------------------------------------------
         */
        // Trình duyệt tự nạp lại đường dẫn danh sách để người quản trị nhìn thấy dòng dữ liệu nhân viên mới sửa đổi
        return "redirect:/admin/employees";
    }
}
