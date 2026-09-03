package systemanagercv.example.systemanagercv.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    //DTO Dùng để trả dữ liệu User từ Backend -> giao diện/API
    //Nó k phải Entity, k lưu vào database
    private Long id;

    private String username;

    private String email;

    private Boolean enabled;

    private Long roleId;

    private String roleName;

    private String roleDescription;

    //Tại sao UserResponse chỉ có những field này?
    //Vì API danh sách User chỉ cần trả: id, uername,email,enabled,role
    //Không trả: password, userRoles, employee để tránh lộ dữ liệu và tránh kéo theo các quan hệ Entity k cần thiết
}
