package systemanagercv.example.systemanagercv.user.controller;
import jakarta.validation.Valid; // Thư viện bắt buộc import để kích hoạt kiểm duyệt

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult; // Thư viện bắt buộc để hứng lỗi Validation
import org.springframework.web.bind.annotation.*;
import systemanagercv.example.systemanagercv.common.response.ApiResponse;
import systemanagercv.example.systemanagercv.user.dto.request.UserCreateRequest;
import systemanagercv.example.systemanagercv.user.dto.request.UserSearchRequest;
import systemanagercv.example.systemanagercv.user.dto.request.UserUpdateRequest;
import systemanagercv.example.systemanagercv.user.dto.response.UserDetailResponse;
import systemanagercv.example.systemanagercv.user.dto.response.UserResponse;
import systemanagercv.example.systemanagercv.user.service.UserService;

@RestController// Đánh dấu đây là Cửa ngõ tiếp nhận API (Trả về dữ liệu JSON)
@RequestMapping("/api/v1/users") // Địa chỉ đường dẫn gốc cho toàn bộ các API quản lý User
@RequiredArgsConstructor// Tự động tiêm UserService vào để sử dụng và tự động viết hộ một hàm khởi tạo (Constructor) chứa tất cả các biến được khai báo với từ khóa final
public class UserController {

    private final UserService userService;

    // =========================================================
    // 1. API: TÌM KIẾM + PHÂN TRANG + SẮP XẾP
    // Hành động: GET -> Địa chỉ: /api/v1/users (UserResponse)
    // =========================================================
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> search(
            @Valid UserSearchRequest request // Nhận các tham số tìm kiếm (keyword, page, size) từ URL và kiểm tra hợp lệ
    ){
        //Gọi Service để thực hiện quét Database và phân trang
        Page<UserResponse> result = userService.search(request);

        //Đóng gói kết quả vào chiếc hộp vạn năng ApiResponse đã làm
        ApiResponse<Page<UserResponse>> response = new ApiResponse<>(
                "success",
                "Success",
                result
        );
        //Trả về cho FE với trạng thái HTTP 200 OK kèm chiếc hộp response
        return ResponseEntity.ok(response);
    }


    // =========================================================
    // 2. API: XEM CHI TIẾT MỘT USER THEO ID
    // Hành động: GET -> Địa chỉ: /api/v1/users/{id} (Ví dụ: /api/v1/users/5) (UserDetailResponse)
    // =========================================================
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> findById(
            @PathVariable Long id  //@PathVariable giúp lấy số ID từ trên đường dẫn URL xuống biến 'id' này
    ){

        //Gọi Service tìm kiếm ông User theo ID này
        UserDetailResponse result = userService.findById(id);

        //Đóng gói dữ liệu chi tiết vào chiếc hộp ApiResponse
        ApiResponse<UserDetailResponse> response = new ApiResponse<>(
                "success",
                "Success",
                result
        );
        return ResponseEntity.ok(response);
    }


    // =========================================================
    // 3. API: TẠO MỚI NGƯỜI DÙNG (ĐĂNG KÝ)
    // Hành động: POST -> Địa chỉ: /api/v1/users
    // =========================================================
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Valid @RequestBody UserCreateRequest request // @RequestBody để nhận gói dữ liệu JSON người dùng gửi lên
    ){
        // Gọi Service chạy chuỗi logic 11 bước tạo mới tài khoản (UserServiceImpl -> createUser)
        UserResponse result = userService.createUser(request);

        //Đóng gói thông tin tài khoản vừa tạo thành công vào ApiResponse
        ApiResponse<UserResponse> response = new ApiResponse<>(
                "success",
                "User created Successfully",
                result
        );
        // Trả về kết quả với mã trạng thái HTTP 201 (CREATED - Đã tạo mới thành công)
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // =========================================================
    // 4. API: CẬP NHẬT THÔNG TIN NGƯỜI DÙNG
    // Hành động: PUT -> Địa chỉ: /api/v1/users/{id} (Ví dụ: /api/v1/users/5)
    // =========================================================
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable Long id,                         // Lấy ID của người cần sửa trên URL
            @Valid @RequestBody UserUpdateRequest request // Lấy gói thông tin mới trong phần thân (Body) của request
    ){
        //Gọi Service chạy chuỗi logic cập nhật thông tin loại trừ
        UserResponse result = userService.updateUser(id, request);

        //Đóng gói kết quả mới vào ApiResponse
        ApiResponse<UserResponse> response = new ApiResponse<>(
                "success",
                "User update successfully",
                result
        );
        return ResponseEntity.ok(response);
    }


    // =========================================================
    // 5. API: XÓA NGƯỜI DÙNG
    // Hành động: DELETE -> Địa chỉ: /api/v1/users/{id}
    // =========================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id // Lấy ID của người cần xóa
    ){
        //Gọi Service thực hiện hành động xóa
        userService.delete(id);

        //Vì là hành động xóa, không cần trả về dữ liệu gì nữa nên ô data để 'null'
        //Kiểu dữ liệu bọc là ApiResponse<Void> nghĩa là chiếc hộp rỗng dữ liệu
        ApiResponse<Void> response = new ApiResponse<>(
                "success",
                "User deleted successfully",
                null
        );
        return ResponseEntity.ok(response);
    }
}
