package systemanagercv.example.systemanagercv.employee.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import systemanagercv.example.systemanagercv.department.entity.Departments;
import systemanagercv.example.systemanagercv.employee.entity.Employee;
import systemanagercv.example.systemanagercv.employee.enums.EmployeePosition;
import systemanagercv.example.systemanagercv.employee.enums.EmployeeStatus;
import systemanagercv.example.systemanagercv.user.entity.User;
import systemanagercv.example.systemanagercv.department.service.DepartmentService;
import systemanagercv.example.systemanagercv.employee.service.EmployeeService;
import systemanagercv.example.systemanagercv.user.service.UserService;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final UserService userService;
    private final DepartmentService departmentService;

    @GetMapping("/employees")
    public String index(
        Model model,
        //Từ khóa tìm kiếm
        //@RequestParam : Trích xuất (lấy) các tham số được truyền trên thanh địa chỉ URL từ trình duyệt gửi nên
        @RequestParam(name = "keyword", defaultValue = "") String keyword,
        //Số trang mặc định là trang 1
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo

    ) {
        Page<Employee> page;
        // =====================================================
        // NẾU CÓ TỪ KHÓA → TÌM KIẾM + PHÂN TRANG
        // =====================================================
        if (keyword != null && !keyword.trim().isEmpty()){
            page = employeeService.searchEmployee(keyword.trim(), pageNo);

            // =====================================================
            // NẾU KHÔNG CÓ TỪ KHÓA → CHỈ PHÂN TRANG
            // =====================================================
        }else {
            page = employeeService.getAll(pageNo);
        }
        // =====================================================
        // ĐƯA DỮ LIỆU RA VIEW
        // =====================================================

        // Danh sách nhân viên của trang hiện tại
        model.addAttribute( "list", page.getContent() );

        // ==========================================
        // ENUM về dịnh dạng Position và Status Employee
        // ==========================================

        model.addAttribute(
                "positions",
                EmployeePosition.values()
        );

        model.addAttribute(
                "statuses",
                EmployeeStatus.values()
        );

        // Từ khóa tìm kiếm
        // Giữ lại keyword khi chuyển trang
        model.addAttribute("keyword", keyword);

        // Trang hiện tại
        model.addAttribute("currentPage", pageNo);

        // Tổng số trang
        model.addAttribute("totalPages", page.getTotalPages());

        // Tổng số nhân viên
        model.addAttribute("totalItems", page.getTotalElements());

        // Số nhân viên trên mỗi trang
        model.addAttribute( "pageSize", page.getSize() );


        return "admin/employee/index";
    }

    /**
     * 2. HIỂN THỊ TRANG THÊM MỚI NHÂN VIÊN
     */
    /**
     * Hiển thị form thêm nhân viên
     * URL: /admin/employee/add-employee
     */
    @GetMapping("/employee/add-employee")
    public String showAddForm(Model model) {

        model.addAttribute("employee", new Employee());

        model.addAttribute("users", userService.getEmployeeUsers());

        model.addAttribute("departments", departmentService.getAll());

        // ENUM POSITION
        model.addAttribute(
                "positions",
                EmployeePosition.values()
        );

        // ENUM STATUS
        model.addAttribute(
                "statuses",
                EmployeeStatus.values()
        );

        return "admin/employee/add";
    }
    /** * =========================================================
     * * 3. XỬ LÝ THÊM NHÂN VIÊN *
     * =========================================================
     * * * URL: * POST /admin/employees/add */
    @PostMapping("/employees/add")
    public String saveEmployee(
            @RequestParam("userId") Long userId,
            @RequestParam("departmentId") Long departmentId,
            @ModelAttribute("employee") Employee employee,
            RedirectAttributes redirectAttributes
    ) {

        try {

            // =====================================================
            // 1. KIỂM TRA MÃ NHÂN VIÊN
            // =====================================================

            if (employeeService.existsByEmployeeCode(
                    employee.getEmployeeCode())) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Mã nhân viên đã tồn tại!"
                );

                return "redirect:/admin/employee/add-employee";
            }


            // =====================================================
            // 2. KIỂM TRA USER
            // =====================================================

            User user = userService.findById(userId);

            if (user == null) {
                throw new RuntimeException(
                        "Không tìm thấy tài khoản!"
                );
            }


            // =====================================================
            // 3. USER ĐÃ CÓ EMPLOYEE?
            // =====================================================

            if (employeeService.existsByUserId(userId)) {

                throw new RuntimeException(
                        "Tài khoản này đã được liên kết với nhân viên khác!"
                );
            }


            // =====================================================
            // 4. USER PHẢI CÓ ROLE EMPLOYEE
            // =====================================================

            boolean isEmployee = user.getUserRoles()
                    .stream()
                    .anyMatch(userRole ->
                            userRole.getRole() != null
                                    && "EMPLOYEE".equals(
                                    userRole.getRole().getName()
                            )
                    );

            if (!isEmployee) {

                throw new RuntimeException(
                        "Tài khoản không có quyền EMPLOYEE!"
                );
            }


            // =====================================================
            // 5. LẤY PHÒNG BAN
            // =====================================================

            Departments department =
                    departmentService.findById(departmentId);

            if (department == null) {

                throw new RuntimeException(
                        "Không tìm thấy phòng ban!"
                );
            }


            // =====================================================
            // 6. GÁN QUAN HỆ
            // =====================================================

            employee.setUser(user);
            employee.setDepartment(department);


            // =====================================================
            // 7. LƯU
            // =====================================================

            employeeService.save(employee);


            redirectAttributes.addFlashAttribute(
                    "success",
                    "Thêm nhân viên thành công!"
            );

            return "redirect:/admin/employees";

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/admin/employee/add-employee";
        }
    }

    //edit
    @GetMapping("/employee/edit/{id}")
    public String editEmployee(
            @PathVariable Long id,
            Model model
    ) {

        Employee employee = employeeService.findById(id);

        if (employee == null) {
            return "redirect:/admin/employees";
        }

        // Employee hiện tại
        model.addAttribute(
                "employee",
                employee
        );

        // User có role EMPLOYEE
        // + User hiện tại đang được Employee sử dụng
        model.addAttribute(
                "users",
                userService.getEmployeeUsersForEdit(id)
        );

        // Danh sách phòng ban
        model.addAttribute(
                "departments",
                departmentService.getAll()
        );

        // ==========================================
        // ENUM POSITION
        // ==========================================

        model.addAttribute(
                "positions",
                EmployeePosition.values()
        );

        // ==========================================
        // ENUM STATUS
        // ==========================================

        model.addAttribute(
                "statuses",
                EmployeeStatus.values()
        );

        return "admin/employee/edit";
    }

    @PostMapping("/employee/update/{id}")
    public String updateEmployee(
            @PathVariable Long id,

            @RequestParam("userId") Long userId,

            @RequestParam("departmentId") Long departmentId,

            @ModelAttribute("employee") Employee employee,

            RedirectAttributes redirectAttributes
    ) {

        try {

            // ==========================================
            // LẤY USER
            // ==========================================

            User user = userService.findById(userId);

            // ==========================================
            // LẤY PHÒNG BAN
            // ==========================================

            Departments department =
                    departmentService.findById(departmentId);

            // ==========================================
            // GÁN QUAN HỆ
            // ==========================================

            employee.setUser(user);

            employee.setDepartment(department);

            // ==========================================
            // UPDATE
            // ==========================================

            employeeService.update(id, employee);

            // ==========================================
            // THÔNG BÁO THÀNH CÔNG
            // ==========================================

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Cập nhật nhân viên thành công!"
            );

            return "redirect:/admin/employees";

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Cập nhật nhân viên thất bại: " + e.getMessage()
            );

            return "redirect:/admin/employee/edit/" + id;
        }
    }
    @GetMapping("/employee/delete/{id}")
    public String deleteEmployee(@PathVariable Long id) {
        employeeService.delete(id);
        return "redirect:/admin/employees";
    }
}
