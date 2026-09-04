package systemanagercv.example.systemanagercv.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDetailResponse {

    private Long id;

    private String username;

    private String email;

    private Boolean enabled;

    private Long roleId;

    private String roleName;

    private String roleDescription;

    private Boolean hasEmployee;
}

/*Tại sao lại phải cần cả UserResponse và UserDetailsResponse?
* Vì đây là cách thiết kế phổ biến:
* - UserResponse: dùng cho danh sách User (GET /api/v1/users): {
    "id": 1,
    "username": "admin",
    "email": "admin@gmail.com",
    "enabled": true,
    "roleId": 1,
    "roleName": "ADMIN",
    "roleDescription": "Administrator"
}
*
* - UserDetailResponse: dùng cho chi tiết của 1 User (GET /api/v1/users/1)
* có thể có thêm: hasEmployee để biết được liên kết với Employee chưa*/