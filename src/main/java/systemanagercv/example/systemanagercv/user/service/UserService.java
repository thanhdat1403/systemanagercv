package systemanagercv.example.systemanagercv.user.service;

import org.springframework.data.domain.Page;
import systemanagercv.example.systemanagercv.user.dto.request.UserCreateRequest;
import systemanagercv.example.systemanagercv.user.dto.request.UserSearchRequest;
import systemanagercv.example.systemanagercv.user.dto.request.UserUpdateRequest;
import systemanagercv.example.systemanagercv.user.dto.response.UserDetailResponse;
import systemanagercv.example.systemanagercv.user.dto.response.UserResponse;
import systemanagercv.example.systemanagercv.user.dto.response.UserSelectResponse;
import systemanagercv.example.systemanagercv.user.entity.User;

import java.util.List;

public interface UserService {

    // =====================================================
    // USER MANAGEMENT
    // =====================================================

    List<UserResponse> getAll();
    UserDetailResponse findById(Long id);

    // Dùng cho Spring Security khi đăng nhập
    User findByUsername(String username);

    User findActiveUserEntityById(Long id);

    // =====================================================
    // CREATE / UPDATE / DELETE
    // =====================================================
    //Để Service xử lý nghiệp vụ vì để cho Admin khi tạo User, phải đồng thời tạo UserRole
    //User createUser(User user, Long roleId); // K nên làm theo như này vì đang trả thẳng ra Entity, Theo (Quy định 43): Không trả Entity trực tiếp cho API, phải luôn dùng DTO
    UserResponse createUser(UserCreateRequest request); // Ta đã có UserCreateRequest nên phải làm như này: Để tránh lội dữ liệu và giảm phụ thuộc vào tầng dữ liệu

    //Tương tự với update user cũng phải tuân thủ vì ta đã có UserUpdateRequest
    UserResponse updateUser(
            Long id,
            UserUpdateRequest request
    );

    void delete(Long id);

    // ============================================================
    // EMPLOYEE - USER SELECT
    // ============================================================

    /**
     * Lấy User có thể chọn khi thêm Employee.
     *
     * Bao gồm:
     * - HR
     * - TECH_LEAD
     * - EMPLOYEE
     *
     * Không bao gồm:
     * - ADMIN
     * - User đã được liên kết Employee
     * - User disabled
     * - User đã soft delete
     */
    List<UserSelectResponse> getEmployeeUsers();

    /**
     * Lấy User có thể chọn khi sửa Employee.
     *
     * Bao gồm cả User hiện tại đang được liên kết
     * với Employee đang sửa.
     */
    List<UserSelectResponse> getEmployeeUsersForEdit(Long employeeId);


    // =====================================================
    // SEARCH / PAGINATION
    // =====================================================
    //Phân trang + tìm kiếm với user
    Page<UserResponse> search(UserSearchRequest request);
}