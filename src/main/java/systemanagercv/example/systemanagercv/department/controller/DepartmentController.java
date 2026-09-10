package systemanagercv.example.systemanagercv.department.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Bắt buộc import thư viện này để truyền dữ liệu ra HTML
import org.springframework.web.bind.annotation.*;
import systemanagercv.example.systemanagercv.common.response.ApiResponse;
import systemanagercv.example.systemanagercv.department.dto.request.DepartmentCreateRequest;
import systemanagercv.example.systemanagercv.department.dto.request.DepartmentSearchRequest;
import systemanagercv.example.systemanagercv.department.dto.request.DepartmentUpdateRequest;
import systemanagercv.example.systemanagercv.department.dto.response.DepartmentDetailResponse;
import systemanagercv.example.systemanagercv.department.dto.response.DepartmentResponse;
import systemanagercv.example.systemanagercv.department.entity.Departments;
import systemanagercv.example.systemanagercv.department.enums.DepartmentStatus;
import systemanagercv.example.systemanagercv.department.service.DepartmentService;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    // =========================================================
    // 1. API: TÌM KIẾM NÂNG CAO + PHÂN TRANG + SẮP XẾP PHÒNG BAN
    // Hành động: GET -> Địa chỉ: /api/v1/departments
    // Ví dụ: GET /api/v1/departments?keyword=IT&page=0&size=10&status=ACTIVE
    // =========================================================
    @GetMapping
    public ResponseEntity<ApiResponse<Page<DepartmentResponse>>> search(
            @Valid DepartmentSearchRequest request // Nhận các tham số lọc gửi qua URL và tự động kiểm tra tính hợp lệ dữ liệu (@Valid)
    ){
        //Chuyển gói yêu cầu xuống service để ghép nối Specification Lego và lấy trang dữ liệu lên
        Page<DepartmentResponse> result =
                departmentService.search(request);

        //Đóng gói trang dữ liệu kết quả sạch đẹp vào khuôn mẫu ApiResponse
        ApiResponse<Page<DepartmentResponse>> response =
                new ApiResponse<>(
                        "success",
                        "Success",
                        result
                );
        //Phản hồi kết quả về cho FE với mã trạng thái HTTP 200 OK
        return ResponseEntity.ok(response);
    }

    // =========================================================
    // 2. API: XEM CHI TIẾT MỘT PHÒNG BAN THEO ID
    // Hành động: GET -> Địa chỉ: /api/v1/departments/{id}
    // Ví dụ: GET /api/v1/departments/5
    // =========================================================
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentDetailResponse>> findById(
            @PathVariable Long id // @PathVariable giúp tự động bốc số ID nằm ngay trên thanh URL gán vào biến 'id'
    ){
        // Gọi Service tìm kiếm thông tin chi tiết phòng ban (Hàm có chốt chặn filter loại bỏ bản ghi xóa mềm)
        DepartmentDetailResponse result =
                departmentService.findById(id);

        //Đóng gói dữ liệu chi tiết sạch đẹp vào chiếc hộp ApiResponse
        ApiResponse<DepartmentDetailResponse> response =
                new ApiResponse<>(
                        "success",
                        "Success",
                        result
                );
        return ResponseEntity.ok(response);
    }

    // =========================================================
    // 3. API: TẠO MỚI PHÒNG BAN (THÊM PHÒNG BAN)
    // Hành động: POST -> Địa chỉ: /api/v1/departments
    // =========================================================
    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentResponse>> create(
            @Valid @RequestBody DepartmentCreateRequest request // @RequestBody ép hệ thống mở gói JSON gửi lên đổ vào Java Object
    ){
        // Chuyển thông tin xuống Service thực hiện cắt khoảng trắng mã code, kiểm tra trùng lặp và lưu DB
        DepartmentResponse result =
                departmentService.create(request);

        // Đóng gói dữ liệu phòng ban vừa tạo thành công kèm thông báo phản hồi lịch sự
        ApiResponse<DepartmentResponse> response =
                new ApiResponse<>(
                        "success",
                        "Department created successfully",
                        result
                );

        // Trả kết quả về với trạng thái HTTP 201 (CREATED - Đã tạo thành công gói tài nguyên)
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);

    }

    // =========================================================
    // 4. API: CẬP NHẬT THÔNG TIN PHÒNG BAN
    // Hành động: PUT -> Địa chỉ: /api/v1/departments/{id}
    // Ví dụ: PUT /api/v1/departments/5
    // =========================================================
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> update(
            @PathVariable Long id, //Lấy mã ID của phòng ban cần chỉnh sửa
            @Valid @RequestBody DepartmentUpdateRequest request // Lấy gói JSON chứa các thông tin chỉnh sửa mới
    ){
        //Gọi Service thực hiện cập nhật thông tin và kiểm tra trùng mã có loại trừ chính mình
        DepartmentResponse result =
                departmentService.update(id, request);

        // Đóng gói kết quả cập nhật mới nhất vào chiếc hộp ApiResponse
        ApiResponse<DepartmentResponse> response =
                new ApiResponse<>(
                        "success",
                        "Department updated successfully",
                        result
                );
        return ResponseEntity.ok(response);

    }

    // =========================================================
    // 5. API: XÓA PHÒNG BAN
    // Hành động: DELETE -> Địa chỉ: /api/v1/departments/{id}
    // Lưu ý: Tầng Service thực hiện XÓA MỀM (SOFT DELETE), chỉ chuyển cờ 'deleted' thành true chứ không xóa vật lý.
    // =========================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id
    ){
        //Gọi Service kích hoạt tính năng đổi cờ trạng thái ẩn phòng ban này đi
        departmentService.delete(id);

        //Vì phòng ban đã bị xóa nên không cần trả dữ liệu gì v nữa, trường 'data' cuối cùng để là null
        // Kiểu bọc ApiResponse<Void> đại diện cho chiếc hộp rỗng dữ liệu
        ApiResponse<Void> response =
                new ApiResponse<>(
                        "success",
                        "Department deleted successfully",
                        null
                );
        return ResponseEntity.ok(response);
    }
}
