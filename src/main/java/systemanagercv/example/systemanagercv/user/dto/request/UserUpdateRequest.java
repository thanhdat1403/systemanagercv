package systemanagercv.example.systemanagercv.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {

    // =====================================================
    // USERNAME
    // =====================================================

    @NotBlank(message = "error.user.username.required")
    @Size(
            max = 100,
            message = "error.user.username.maxLength"
    )
    private String username;

    // =====================================================
    // FULL NAME
    // =====================================================

    @NotBlank(message = "error.user.fullName.required")
    @Size(
            max = 255,
            message = "error.user.fullName.maxLength"
    )
    private String fullName;

    // =====================================================
    // EMAIL
    // =====================================================

    @Email(message = "error.user.email.invalid")
    @Size(
            max = 255,
            message = "error.user.email.maxLength"
    )
    private String email;

    // =====================================================
    // PASSWORD
    // =====================================================

    /*
     * @Pattern: Kiểm tra dữ liệu bằng biểu thức chính quy.
     *
     * ^$          : cho phép chuỗi rỗng
     * |           : hoặc
     * ^.{6,255}$  : chuỗi có từ 6 đến 255 ký tự
     *
     * Khi update User:
     * - Để trống password → giữ nguyên password cũ.
     * - Nhập password mới → phải có từ 6 đến 255 ký tự.
     */
    @Pattern(
            regexp = "^$|^.{6,255}$",
            message = "error.user.password.invalidLength"
    )
    private String password;

    // =====================================================
    // ROLE
    // =====================================================

    @NotNull(message = "error.user.role.required")
    private Long roleId;

    // =====================================================
    // ENABLED
    // =====================================================

    @NotNull(message = "error.user.enabled.required")
    private Boolean enabled;
}