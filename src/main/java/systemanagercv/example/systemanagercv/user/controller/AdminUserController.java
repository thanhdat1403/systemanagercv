package systemanagercv.example.systemanagercv.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import systemanagercv.example.systemanagercv.role.entity.Role;
import systemanagercv.example.systemanagercv.role.service.RoleService;
import systemanagercv.example.systemanagercv.user.dto.request.UserCreateRequest;
import systemanagercv.example.systemanagercv.user.dto.request.UserSearchRequest;
import systemanagercv.example.systemanagercv.user.dto.request.UserUpdateRequest;
import systemanagercv.example.systemanagercv.user.dto.response.UserDetailResponse;
import systemanagercv.example.systemanagercv.user.dto.response.UserResponse;
import systemanagercv.example.systemanagercv.user.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor// Tự động tiêm UserService vào để sử dụng và tự động viết hộ một hàm khởi tạo (Constructor) chứa tất cả các biến được khai báo với từ khóa final
public class AdminUserController {

    private final UserService userService;
    private final RoleService roleService;

    // =========================================================
    // 1. TRANG DANH SÁCH USER (CÓ TÌM KIẾM + PHÂN TRANG)
    // Đường dẫn: GET: /admin/users
    // =========================================================
    @GetMapping
    public String index(
            @Valid @ModelAttribute("searchRequest") UserSearchRequest request, // Nhận các điều kiện lọc (từ khóa, số trang, quyền...) từ Form giao diện gửi lên
            BindingResult bindingResult,                      // Cuốn sổ ghi lại kết quả kiểm tra dữ liệu xem có bị lỗi gì không
            Model model                                       // Chiếc giỏ dùng để chuyển dữ liệu từ code Java sang cho file HTML giao diện
    ) {
        //Nếu cuốn sổ ghi lỗi phát hiện ng dùng nhập sai quy định (Ví dụ: nhập số trang là chữ cái, nhập số lượng dòng là số âm...)
        if (bindingResult.hasErrors()) {
            //Lập tức reset (Khởi tạo lại) request về mặc định trống rỗng để tránh làm sập web
            request = new UserSearchRequest();
        }

        //Gọi bếp (Service) quét database lấy dữ liệu phân trang và danh sách quyền
        Page<UserResponse> userPage = userService.search(request);
        List<Role> roles = roleService.getAll();

        //Đút dữ liệu thực tế và danh sách quyền vào giỏ
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("roles", roles);

        model.addAttribute(
                "searchRequest",
                request
        );

        //Đẩy thông số kích thước trang và cách sắp xếp sang HTML
        model.addAttribute("pageSize", request.getSize());
        model.addAttribute("sortBy", request.getSortBy());

        //Spring tính trang từ số 0, giao diện hiển thị tính từ số 1 nên phải cộng thêm 1
        model.addAttribute("currentPage", userPage.getNumber() + 1);
        model.addAttribute("totalPages", userPage.getTotalPages()); //Tổng số trang đang có
        model.addAttribute("totalItems", userPage.getTotalElements()); //Tổng số bản ghi trong DB
        return "admin/users/index";
    }

    // =========================================================
    // 2. HIỂN THỊ MÀN HÌNH FORM THÊM MỚI USER
    // Đường dẫn: GET: /admin/users/add
    // =========================================================
    @GetMapping("/add")
    public String add(Model model) {
        //Khởi tại một form trống (Request DTO) để làm khuôn mẫu cho các ô nhập liệu trên HTML gắn vào
        UserCreateRequest request = new UserCreateRequest();

        model.addAttribute("user", request); //Gửi khuôn form trống sang giao diện trống
        model.addAttribute("roles", roleService.getAll()); //Gửi danh sách quyền để hiển thị ô chọn (Select Box)

        return "admin/users/add";
    }

    // =========================================================
    // 3. XỬ LÝ KHI BẤM NÚT "LƯU" THÊM MỚI USER
    // Đường dẫn: POST: /admin/users/add
    // =========================================================
    @PostMapping("/add")
    public String create(
            @Valid @ModelAttribute("user") UserCreateRequest request, //Nhận toàn bộ dữ liệu người dùng vừa điền từ Form
            BindingResult bindingResult, // Sổ kiểm tra lỗi nhập liệu (Ví dụ: Email trống, mật khẩu ngắn)
            Model model,
            RedirectAttributes redirectAttributes // Công cụ tạo dòng thông báo nhanh (Alert) hiện lên rồi tự biến mất
    ) {
        //Nếu người dùng điền thiếu thông tin hoặc sai định dạng quy định
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", roleService.getAll()); //Nạp lại danh sách quyền cho ô chọn
            return "admin/users/add"; //Giữ nguời dùng ở lại trang nhập liệu để họ nhìn thấy lỗi và sửa lại
        }
        //Nếu thông tin điền đúng hết -> Gọi Service lưu tài khoản vào Database (Logic 11 bước đã học)
        userService.createUser(request);

        //Tạo một thông báo xanh hiện lên màn hình sau khi chuyển hướng
        redirectAttributes.addFlashAttribute("message", "Thêm người dùng thành công!");

        //Ra lệnh cho trình duyệt tự chuyển hướng quay về trang danh sách chính để xem kết quả mới
        return "redirect:/admin/users";
    }

    // =========================================================
    // 4. HIỂN THỊ MÀN HÌNH FORM CẬP NHẬT (SỬA) USER
    // Đường dẫn: GET: /admin/users/edit/{id}
    // =========================================================
    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id, //Bốc số ID của user cần sửa từ trên đường dẫn URL xuống
            Model model
    ){
        //Vào database tìm thông tin chi tiết hiện tại của User đó theo ID
        UserDetailResponse user = userService.findById(id); //UserDetailResponse: là để lấy ra chi tiết của 1 user để edit

        //Tạo khuôn Form sửa đổi và đổ dữ liệu cũ của User đó vào các ô nhập liệu
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername(user.getUsername());
        request.setEmail(user.getEmail());
        request.setRoleId(user.getRoleId());
        request.setEnabled(user.getEnabled());

        model.addAttribute("user", request); //Gửi form chứa dữ liệu cũ sang giao diện
        model.addAttribute("userId", id);  //Giữ lại ID để biết đang sửa cho ai
        model.addAttribute("roles", roleService.getAll()); //Lấy danh sách quyền để chọn lại quyền
        return "admin/users/edit";
    }

    // =========================================================
    // 5. XỬ LÝ KHI BẤM NÚT "LƯU" CẬP NHẬT USER
    // Đường dẫn: POST: /admin/users/update/{id}
    // =========================================================
    @PostMapping("/update/{id}")
    public String update(
            @PathVariable Long id, //Lấy ID người cần sửa
            @Valid @ModelAttribute("user") UserUpdateRequest request,// Nhận các thông tin mới do người dùng vừa sửa trên Form
            BindingResult bindingResult, //Kiểm tra lỗi nhập liệu form sửa
            Model model,
            RedirectAttributes redirectAttributes
    ){
        //Nếu sửa thông tin bị lỗi quy định (Ví dụ xóa trống email)
        if (bindingResult.hasErrors()) {
            model.addAttribute("userId", id);
            model.addAttribute("roles", roleService.getAll());
            return "admin/users/edit"; // giữ họ lại ở trang sửa để sửa cho đúng
        }

        //Nếu thông tin sửa đúng quy định -> Gọi Service chạy logic cập nhật loại trừ chính mình
        userService.updateUser(id, request);

        //Tạo thông báo cập nhật thành công dạng Alert
        redirectAttributes.addFlashAttribute("success", "Cập nhật người dùng thành công!");

        //Sửa xong tự động quay về trang danh sách chính
        return "redirect:/admin/users";
    }

    // =========================================================
    // 6. XỬ LÝ KHI BẤM NÚT THÙNG RÁC "XÓA" USER TRÊN GIAO DIỆN
    // Đường dẫn: POST: /admin/users/delete/{id}
    // =========================================================
    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id, // Lấy ID người cần xóa từ nút bấm gửi lên
            RedirectAttributes redirectAttributes
    ){
        //Gọi Service thực hiện hành động xóa
        userService.delete(id);

        //Tạo thông báo xóa thành công dạng Alert
        redirectAttributes.addFlashAttribute("success", "Xóa người dùng thành công!");

        // Xóa xong tự động tải lại trang danh sách chính
        return "redirect:/admin/users";
    }
}
