package systemanagercv.example.systemanagercv.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {

    @NotBlank(message = "error.user.username.required")
    @Size(
            max = 100,
            message = "error.user.username.maxLength"
    )
    private String username;

    @Email(message = "error.user.email.invalid")
    @Size(
            max = 255,
            message = "error.user.email.maxLength"
    )
    private String email;

    /*
     * Khi update:
     * - Để trống password → giữ password cũ
     * - Nhập password mới → mã hóa BCrypt và cập nhật
     */
    @Size(
            min = 6,
            max = 255,
            message = "error.user.password.invalidLength"
    )
    private String password;

    @NotNull(message = "error.user.role.required")
    private Long roleId;

    @NotNull(message = "error.user.enabled.required")
    private Boolean enabled;
}