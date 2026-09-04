package systemanagercv.example.systemanagercv.user.service;

import org.springframework.data.domain.Page;
import systemanagercv.example.systemanagercv.user.dto.request.UserCreateRequest;
import systemanagercv.example.systemanagercv.user.dto.request.UserSearchRequest;
import systemanagercv.example.systemanagercv.user.dto.request.UserUpdateRequest;
import systemanagercv.example.systemanagercv.user.dto.response.UserDetailResponse;
import systemanagercv.example.systemanagercv.user.dto.response.UserResponse;
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

    // =====================================================
    // EMPLOYEE - USER
    // =====================================================
    List<User> getEmployeeUsers();

    // User EMPLOYEE chưa được liên kết
    // + User hiện tại của Employee đang sửa
    List<User> getEmployeeUsersForEdit(Long employeeId);


    // =====================================================
    // SEARCH / PAGINATION
    // =====================================================
    //Phân trang + tìm kiếm với user
    Page<UserResponse> search(UserSearchRequest request);
}