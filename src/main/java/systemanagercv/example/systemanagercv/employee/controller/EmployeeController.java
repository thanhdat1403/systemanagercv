package systemanagercv.example.systemanagercv.employee.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import systemanagercv.example.systemanagercv.common.response.ApiResponse;
import systemanagercv.example.systemanagercv.employee.dto.request.EmployeeCreateRequest;
import systemanagercv.example.systemanagercv.employee.dto.request.EmployeeSearchRequest;
import systemanagercv.example.systemanagercv.employee.dto.request.EmployeeUpdateRequest;
import systemanagercv.example.systemanagercv.employee.dto.response.EmployeeDetailResponse;
import systemanagercv.example.systemanagercv.employee.dto.response.EmployeeResponse;
import systemanagercv.example.systemanagercv.employee.service.EmployeeService;


@RestController // Đánh dấu đây là Cửa ngõ tiếp nhận API, mọi kết quả trả về sẽ tự động biến đổi thành chuỗi dữ liệu JSON
@RequestMapping("/api/v1/employees") // Thiết lập đường dẫn URL gốc cho toàn bộ các API quản lý Nhân viên trong file này
@RequiredArgsConstructor // Tự động tạo Constructor để tiêm (Inject) lớp EmployeeService vào sử dụng mà không cần @Autowired

//EmployeeController → REST API /api/v1/employees
public class EmployeeController {

    private final EmployeeService employeeService;

    // =========================================================
    // 1. API: TÌM KIẾM + PHÂN TRANG + SẮP XẾP EMPLOYEE
    // Hành động: GET -> Địa chỉ: /api/v1/employees
    // Ví dụ: GET /api/v1/employees?keyword=Nguyen&page=0&size=10
    // =========================================================
    @GetMapping
    @PreAuthorize("""
        hasAnyRole(
            'ADMIN',
            'HR',
            'TECH_LEAD',
            'EMPLOYEE'
        )
        """)// Chỉ các role được phép truy cập Employee API mới được vào method này.
            // Quyền chi tiết theo Employee được kiểm tra tại EmployeeAuthorizationService.
    public ResponseEntity<ApiResponse<Page<EmployeeResponse>>> search(
            @Valid @ModelAttribute EmployeeSearchRequest request // Nhận thông số lọc (từ khóa, số trang, kích thước) từ URL và kiểm tra tính hợp lệ (@Valid)
    ){
        //Chuyển gói request xuống tầng Service để lọc thông tin nâng cao và phân trang từ DB lên
        Page<EmployeeResponse> result =
                employeeService.search(request);

        //Đóng gói trang dữ liệu kết quả vào khuôn mẫu phản hồi ApiResponse quen thuộc
        ApiResponse<Page<EmployeeResponse>> response =
                new ApiResponse<>(
                        "success",
                        "Success",
                        result
                );

        //Trả kết quả về cho client với trạng thái HTTP 200 ok
        return ResponseEntity.ok(response);
    }

    // =========================================================
    // 2. API: XEM CHI TIẾT EMPLOYEE THEO ID
    // Hành động: GET -> Địa chỉ: /api/v1/employees/{id}
    // Ví dụ: GET /api/v1/employees/5
    // =========================================================
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeDetailResponse>> findById(
            @PathVariable Long id // Nhãn @PathVariable giúp tự động bốc số ID nằm trên thanh URL gán vào biến 'id'
    ){
        // Gọi Service tìm kiếm thông tin chi tiết nhân viên (Hàm có chốt chặn orElseThrow báo lỗi nếu k thấy ID)
        EmployeeDetailResponse result =
                employeeService.findById(id);

        // Đóng gói dữ liệu chi tiết sạch đẹp vào chiếc hộp ApiResponse
        ApiResponse<EmployeeDetailResponse> response =
                new ApiResponse<>(
                        "success",
                        "Success",
                        result
                );

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // 3. API: TẠO MỚI EMPLOYEE (THÊM NHÂN VIÊN)
    // Hành động: POST -> Địa chỉ: /api/v1/employees
    // =========================================================
    @PostMapping
    @PreAuthorize("""
    hasAnyRole(
        'ADMIN',
        'HR',
        'TECH_LEAD'
    )
    """)
    public ResponseEntity<ApiResponse<EmployeeResponse>> create(
            @Valid @RequestBody EmployeeCreateRequest request // Nhãn @RequestBody bắt hệ thống mở gói JSON gửi lên đổ vào Java Object
    ){
        // Chuyển thông tin xuống Service thực hiện chuỗi logic 8 bước kiểm tra chéo và lưu vào Database
        EmployeeResponse result =
                employeeService.create(request);

        // Đóng gói thông tin nhân viên vừa tạo thành công kèm thông báo phản hồi
        ApiResponse<EmployeeResponse> response =
                new ApiResponse<>(
                        "success",
                        "Employee created successfully",
                        result
                );

        // Trả kết quả về với trạng thái HTTP 201 (CREATED - Đã tạo mới thành công trong hệ thống)
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);

    }

    // =========================================================
    // 4. API: CẬP NHẬT THÔNG TIN EMPLOYEE
    // Hành động: PUT -> Địa chỉ: /api/v1/employees/{id}
    // Ví dụ: PUT /api/v1/employees/5
    // =========================================================
    @PutMapping("/{id}")
    @PreAuthorize("""
    hasAnyRole(
        'ADMIN',
        'HR',
        'TECH_LEAD',
        'EMPLOYEE'
    )
    """)
    public ResponseEntity<ApiResponse<EmployeeResponse>> update(
            @PathVariable Long id, // Bốc số ID của nhân viên cần chỉnh sửa trên URL
            @Valid @RequestBody EmployeeUpdateRequest request // Bốc dữ liệu JSON chứa các thông tin chỉnh sửa mới
    ){
        // Gọi Service thực hiện cập nhật thông tin và kiểm tra trùng lặp có loại trừ chính mình
        EmployeeResponse result =
                employeeService.update(id, request);

        // Đóng gói kết quả cập nhật mới nhất vào chiếc hộp ApiResponse
        ApiResponse<EmployeeResponse> response =
                new ApiResponse<>(
                        "success",
                        "Employee updated successfully",
                        result
                );

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // 5. API: XÓA NHÂN VIÊN
    // Hành động: DELETE -> Địa chỉ: /api/v1/employees/{id}
    // Lưu ý: Tầng Service thực hiện XÓA MỀM (SOFT DELETE), chỉ chuyển cờ 'deleted' thành true chứ không xóa mất tích trong DB.
    // =========================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id
    ){
        // Gọi Service kích hoạt tính năng đổi cờ trạng thái ẩn nhân viên này đi
        employeeService.delete(id);

        //Vì nhân viên đã bị xóa nên không cần trả dữ liệu gì về nữa, trường 'data' cuối cùng để là null
        // Kiểu bọc ApiResponse<Void> Đại diện cho chiếc hộp trống k chứa ruột dữ liệu
        ApiResponse<Void> response =
                new ApiResponse<>(
                        "success",
                        "Employee deleted successfully",
                        null
                );

        return ResponseEntity.ok(response);
    }
}
