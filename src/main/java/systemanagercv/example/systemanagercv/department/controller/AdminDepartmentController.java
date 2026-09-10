package systemanagercv.example.systemanagercv.department.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import systemanagercv.example.systemanagercv.department.dto.request.DepartmentCreateRequest;
import systemanagercv.example.systemanagercv.department.dto.request.DepartmentSearchRequest;
import systemanagercv.example.systemanagercv.department.dto.request.DepartmentUpdateRequest;
import systemanagercv.example.systemanagercv.department.dto.response.DepartmentResponse;
import systemanagercv.example.systemanagercv.department.enums.DepartmentStatus;
import systemanagercv.example.systemanagercv.department.service.DepartmentService;

@Controller
@RequestMapping("/admin/departments") // Thiết lập đường dẫn URL gốc trên thanh địa chỉ cho trang quản trị phòng ban
@RequiredArgsConstructor

public class AdminDepartmentController {

    private final DepartmentService departmentService;

    // =========================================================
    // 1. TRANG HIỂN THỊ DANH SÁCH PHÒNG BAN (TÌM KIẾM + PHÂN TRANG)
    // Đường dẫn: GET /admin/departments
    // =========================================================
    @GetMapping
    public String index(
            @ModelAttribute DepartmentSearchRequest request,
            Model model
    ) {
        // Gọi Service tìm kiếm + phân trang
        Page<DepartmentResponse> page =
                departmentService.search(request);

        // =========================================================
        // DANH SÁCH PHÒNG BAN
        // =========================================================
        model.addAttribute(
                "departments",
                page.getContent()
        );

        // =========================================================
        // GIỮ LẠI THÔNG TIN SEARCH/FILTER
        // =========================================================
        model.addAttribute(
                "searchRequest",
                request
        );

        // =========================================================
        // THÔNG TIN PHÂN TRANG
        // =========================================================

        // Page của Spring Data bắt đầu từ 0
        // Giao diện bắt đầu từ 1
        model.addAttribute(
                "currentPage",
                page.getNumber() + 1
        );

        model.addAttribute(
                "totalPages",
                page.getTotalPages()
        );

        model.addAttribute(
                "totalItems",
                page.getTotalElements()
        );

        model.addAttribute(
                "pageSize",
                page.getSize()
        );

        // =========================================================
        // DANH SÁCH STATUS CHO FILTER
        // =========================================================
        model.addAttribute(
                "statuses",
                DepartmentStatus.values()
        );

        return "admin/department/index";
    }



    // =========================================================
    // 2. MÀN HÌNH HIỂN THỊ BIỂU MẪU (FORM) THÊM MỚI PHÒNG BAN
    // Đường dẫn: GET /admin/departments/add
    // =========================================================
    @GetMapping("/add")
    public String add(Model model) {
        // Tạo một form trống (Request DTO) để làm khuôn mẫu liên kết với các ô nhập liệu trên giao diện HTML
        DepartmentCreateRequest request =
                new DepartmentCreateRequest();

        // Đặt trạng thái mặc định ban đầu cho phòng ban mới là Hoạt động (ACTIVE)
        request.setStatus(DepartmentStatus.ACTIVE);

        model.addAttribute(
                "department",
                request // Gửi khuôn form trống sang giao diện
        );

        model.addAttribute(
                "statuses",
                DepartmentStatus.values() // Lấy mảng hằng số trạng thái từ file Enum để vẽ ô chọn Select Dropdown
        );

        // Mở hiển thị giao diện file HTML: src/main/resources/templates/admin/department/add.html
        return "admin/department/add";

    }

    // =========================================================
    // 3. XỬ LÝ KHI NGƯỜI DÙNG BẤM NÚT "LƯU" THÊM MỚI PHÒNG BAN
    // Đường dẫn: POST /admin/departments/add
    // =========================================================
    @PostMapping("/add")
    public String create(
            @Valid @ModelAttribute("department") DepartmentCreateRequest request,// Đón nhận gói dữ liệu chứa thông tin do người dùng vừa điền từ Form gửi lên
            BindingResult bindingResult, // Sổ kiểm tra lỗi nhập liệu Form (Ví dụ: Mã/Tên bỏ trống)
            Model model
    ){

        // Nếu người dùng điền thiếu thông tin bắt buộc hoặc nhập sai quy định định dạng form
        if (bindingResult.hasErrors()) {
            // Nạp lại danh sách trạng thái cho ô chọn Select Dropdown (Vì tải lại trang bộ nhớ giỏ cũ sẽ bị dọn dẹp)
            model.addAttribute(
                    "statuses",
                    DepartmentStatus.values()
            );
            // Gữi ở lại trang điền đơn để nhìn thấy các dòng chữ thông báo lỗi màu đỏ và chỉnh sửa tại chỗ
            return "admin/department/add";
        }
        // Nếu thông tin điền đúng quy định -> Gọi Service lưu phòng ban vào Database (Có kiểm tra trùng mã)
        departmentService.create(request);

        // Lưu thành công, ra lệnh trình duyệt tự chuyển hướng quay về trang danh sách chính để cập nhật bảng mới
        return "redirect:/admin/departments";
    }

    // =========================================================
    // 4. MÀN HÌNH HIỂN THỊ BIỂU MẪU (FORM) CHỈNH SỬA PHÒNG BAN
    // Đường dẫn: GET /admin/departments/edit/{id}
    // =========================================================
    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model
    ){
        //Vào database tìm thông tin chi tiết hiện tại của phòng ban đó theo ID (Có loại trừ bản ghi xóa mềm)
        var department =
                departmentService.findById(id);

        // Khởi tạo khuôn form sửa đổi và đổ dữ liệu cũ của phòng ban đó vào các ô nhập liệu trên Web
        DepartmentUpdateRequest request =
                new DepartmentUpdateRequest();

        request.setCode(department.getCode());
        request.setName(department.getName());
        request.setDescription(department.getDescription());
        request.setStatus(department.getStatus());

        model.addAttribute(
                "department",
                request // Gửi Form chứa dữ liệu cũ sang giao diện
        );

        model.addAttribute(
                "departmentId",
                id // Giữ lại ID để biết đang thực hiện sửa cho phòng ban nào
        );

        model.addAttribute(
                "statuses",
                DepartmentStatus.values() // Nạp lại danh sách hằng số trạng thái để chọn lại nếu cần
        );

        return "admin/department/edit";
    }

    // =========================================================
    // 5. XỬ LÝ KHI NGƯỜI DÙNG BẤM NÚT "LƯU" CẬP NHẬT PHÒNG BAN
    // Đường dẫn: POST /admin/departments/edit/{id}
    // =========================================================
    @PostMapping("/edit/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("department") DepartmentUpdateRequest request,
            BindingResult bindingResult,
            Model model
    ){
        // Nếu sửa thông tin bị vi phạm quy định Form (Ví dụ xóa trống tên phòng ban)
        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "departmentId",
                    id
            );
            model.addAttribute(
                    "statuses",
                    DepartmentStatus.values()
            );
            // Gữi họ lại trang sửa để sửa đổi cho đúng quy định
            return "admin/department/edit";
        }
        // Nếu thông tin sửa đúng quy định -> Gọi Service thực hiện lưu cập nhật loại trừ chính mình
        departmentService.update(id, request);

        // Sửa đổi thành công, tự động quay trở về trang danh sách chính
        return "redirect:/admin/departments";
    }

    // =========================================================
    // 6. XỬ LÝ KHI NGƯỜI DÙNG BẤM NÚT THÙNG RÁC "XÓA" PHÒNG BAN TRÊN GIAO DIỆN
    // Đường dẫn: POST /admin/departments/delete/{id}
    // =========================================================
    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id // Lấy ID phòng ban cần xóa từ nút bấm gửi lên
    ) {
        // Gọi Service kích hoạt cờ trạng thái xóa mềm ẩn phòng ban này đi
        departmentService.delete(id);

        // Xóa xong tự động tải lại trang danh sách chính để cập nhật lại bảng dữ liệu
        return "redirect:/admin/departments";
    }
}
