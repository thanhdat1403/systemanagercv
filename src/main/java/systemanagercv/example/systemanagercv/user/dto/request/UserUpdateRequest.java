package systemanagercv.example.systemanagercv.user.dto.request;

import jakarta.validation.constraints.*;
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

    // @Pattern: Kiểm tra dữ liệu bằng Biểu thức chính quy (Regex)
    // Ý nghĩa chuỗi Regex "^$|^.{6,255}$":
    //  - ^$         : Đại diện cho chuỗi RỖNG (độ dài bằng 0)
    //  - |          : Toán tử HOẶC (OR)
    //  - ^.{6,255}$ : Đại diện cho chuỗi CÓ ĐỘ DÀI từ 6 đến 255 ký tự bất kỳ
    /*Tại sao k dùng @Size ở hàm update đc:
    * Vì: Nhãn @Size(min = 6) bắt buộc mật khẩu lúc nào cũng phải có ít nhất 6 ký tự.
    * Trong màn hình Cập nhật (Update), nếu người dùng giữ nguyên mật khẩu cũ (để trống ô mật khẩu) \(\rightarrow \) dữ liệu gửi lên Java sẽ là một chuỗi rỗng "" (độ dài bằng 0). */
    @Pattern(
            regexp = "^$|^.{6,255}$",
            message = "error.user.password.invalidLength" // Câu báo lỗi dịch đa ngôn ngữ nếu vi phạm quy tắc trên
    )
    private String password;


    @NotNull(message = "error.user.role.required")
    private Long roleId;

    @NotNull(message = "error.user.enabled.required")
    private Boolean enabled;
}