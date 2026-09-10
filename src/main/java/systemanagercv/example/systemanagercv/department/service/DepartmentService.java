package systemanagercv.example.systemanagercv.department.service;

import org.springframework.data.domain.Page;
import systemanagercv.example.systemanagercv.department.dto.request.DepartmentCreateRequest;
import systemanagercv.example.systemanagercv.department.dto.request.DepartmentSearchRequest;
import systemanagercv.example.systemanagercv.department.dto.request.DepartmentUpdateRequest;
import systemanagercv.example.systemanagercv.department.dto.response.DepartmentDetailResponse;
import systemanagercv.example.systemanagercv.department.dto.response.DepartmentResponse;
import systemanagercv.example.systemanagercv.department.entity.Departments;

import java.util.List;

public interface DepartmentService {


    /**
     * 1. Tìm kiếm nâng cao + Phân trang + Sắp xếp danh sách Phòng ban.
     * @param request Chứa từ khóa tìm kiếm, số trang, số dòng/trang gửi từ client lên.
     * @return Một trang dữ liệu Page chứa danh sách các DepartmentResponse sạch đẹp.
     */
    Page<DepartmentResponse> search(DepartmentSearchRequest request);

    /**
     * 2. Lấy thông tin chi tiết của một Phòng ban cụ thể theo mã ID.
     * @return Gói dữ liệu chi tiết DepartmentDetailResponse phục vụ hiển thị lên màn hình.
     */
    DepartmentDetailResponse findById(Long id);

    /**
     * 3. Chức năng Thêm mới một Phòng ban vào hệ thống.
     * @param request Gói dữ liệu chứa thông tin phòng ban mới do người dùng điền trên form.
     * @return Thông tin phòng ban sau khi tạo thành công.
     */
    DepartmentResponse create(DepartmentCreateRequest request);

    /**
     * 4. Chức năng Cập nhật (Chỉnh sửa) thông tin Phòng ban đang có sẵn.
     * @param id Mã ID của phòng ban cần chỉnh sửa.
     * @param request Gói dữ liệu chứa các thông tin thay đổi mới.
     * @return Thông tin phòng ban sau khi cập nhật thành công.
     */
    DepartmentResponse update(
            Long id,
            DepartmentUpdateRequest request
    );

    /**
     * 5. Chức năng Xóa phòng ban (Thường triển khai dưới dạng Xóa mềm - Soft Delete).
     * @param id Mã ID của phòng ban cần xóa khỏi hệ thống hiển thị.
     */
    void delete(Long id);

    /**
     * 6. Hàm tiện ích: Tìm và trả về trực tiếp đối tượng thực thể gốc (Entity) của Phòng ban theo ID.
     * Chức năng: Dùng làm cầu nối gọi nội bộ giữa các Service khác với nhau trong hệ thống (Ví dụ: Dùng trong EmployeeService).
     * Tuyệt đối không dùng hàm này để trả dữ liệu trực tiếp ra ngoài API Controller.
     */
    Departments findEntityById(Long id);

    /**
     * Lấy các phòng ban đang hoạt động
     * để hiển thị cho dropdown/select.
     */
    List<DepartmentResponse> getActiveDepartments();
}
