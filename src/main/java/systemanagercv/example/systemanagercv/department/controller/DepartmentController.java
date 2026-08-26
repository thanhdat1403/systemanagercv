package systemanagercv.example.systemanagercv.department.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Bắt buộc import thư viện này để truyền dữ liệu ra HTML
import org.springframework.web.bind.annotation.*;
import systemanagercv.example.systemanagercv.department.entity.Departments;
import systemanagercv.example.systemanagercv.department.service.DepartmentService;

@Controller
@RequestMapping("/admin")
public class DepartmentController {
    @Autowired
    private DepartmentService departmentService;
    /**
     * 1. TRANG DANH SÁCH PHÒNG BAN (ĐƯỜNG DẪN: /admin/department)
     */
    @GetMapping("/department")
    public String index(
        Model model,
        //Từ khóa tìm kiếm
        //@RequestParam : Trích xuất (lấy) các tham số được truyền trên thanh địa chỉ URL từ trình duyệt gửi nên
        @RequestParam(name = "keyword", defaultValue = "") String keyword,
        //Số trang mặc định là trang 1
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo
    ) {
        Page<Departments> page;
        // =====================================================
        // NẾU CÓ TỪ KHÓA → TÌM KIẾM + PHÂN TRANG
        // =====================================================
        if (keyword != null && !keyword.trim().isEmpty()){
            page = departmentService.searchDepartment(keyword.trim(), pageNo);

            // =====================================================
            // NẾU KHÔNG CÓ TỪ KHÓA → CHỈ PHÂN TRANG
            // =====================================================
        }else {
            page = departmentService.getAll(pageNo);
        }

        // =====================================================
        // ĐƯA DỮ LIỆU RA VIEW
        // =====================================================

        // Danh sách phòng ban của trang hiện tại
        model.addAttribute("list", page.getContent());

        // Từ khóa tìm kiếm
        // Giữ lại keyword khi chuyển trang
        model.addAttribute("keyword", keyword);

        // Trang hiện tại
        model.addAttribute("currentPage", pageNo);

        // Tổng số trang
        model.addAttribute("totalPages", page.getTotalPages());

        // Tổng số phòng ban
        model.addAttribute("totalItems", page.getTotalElements());

        // Số phòng ban trên mỗi trang
        model.addAttribute("pageSize", 2); // Có thể thay đổi tùy ý


        return "admin/department/index";
    }
    /**
     * 2. HIỂN THỊ TRANG THÊM MỚI PHÒNG BAN
     */
    /**
     * Hiển thị form thêm phòng ban
     * URL: /admin/department/add-department
     */
    @GetMapping("/department/add-department")
    public String addDepartment(Model model) {
        //Tạo một đối tượng rỗng để liên kết (binding) dữ liệu với form html
        Departments departments = new Departments();
        //Mặc định phòng ban mới là ACTIVE
        departments.setStatus("ACTIVE");
        model.addAttribute("department", departments);
        return "admin/department/add";
    }
    /**
     * 3. XỬ LÝ LỆNH LƯU THÊM MỚI (KHI BẤM NÚT SUBMIT FORM)
     */
    /**
     * Xử lý thêm phòng ban
     * POST: /admin/department/add
     */
    @PostMapping("/department/add")
    public String saveDepartment(@ModelAttribute("department") Departments department) {
        departmentService.create(department);
        return "redirect:/admin/department";
    }
    /**
     * 4. HIỂN THỊ TRANG SỬA PHÒNG BAN THEO ID
     */
    @GetMapping("/department/edit/{id}")
    public String editDepartment(@PathVariable("id") Long id, Model model) {
        //Tìm thông tin phòng ban cũ dựa vào ID trên đường dẫn URL
        Departments department = departmentService.findById(id);
        // Đẩy dữ liệu cũ ra form để người dùng nhìn thấy và chỉnh sửa
        model.addAttribute("department", department);
        return "admin/department/edit";
    }
    /**
     * 5. XỬ LÝ LỆNH CẬP NHẬT (KHI BẤM LƯU SỬA)
     */
    @PostMapping("/department/update-department/{id}")
    public String updateDepartment(@PathVariable("id") Long id, @ModelAttribute("department") Departments department) {
        //Gọi service thực hiện đè dữ liệu mới vào ID cũ dưới DB
        departmentService.update(id, department);
        return "redirect:/admin/department";
    }
    /**
     * 6. XỬ LÝ LỆNH XÓA PHÒNG BAN THEO ID
     */
    @GetMapping("/department/delete/{id}")
    public String deleteDepartment(@PathVariable("id") Long id) {
        //Gọi service thực hiện xóa sạch khỏi DB
        departmentService.delete(id);
        return "redirect:/admin/department";
    }
}
