package systemanagercv.example.systemanagercv.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    // DTO dùng để trả dữ liệu User từ Backend -> giao diện/API

    private Long id;

    private String username;

    private String fullName;

    private String email;

    private Boolean enabled;

    private Long roleId;

    private String roleName;

    private String roleDescription;

    /*
     * Không trả:
     * - password
     * - userRoles
     * - employee
     *
     * để tránh lộ dữ liệu nhạy cảm và tránh kéo theo
     * các quan hệ Entity không cần thiết.
     */
}