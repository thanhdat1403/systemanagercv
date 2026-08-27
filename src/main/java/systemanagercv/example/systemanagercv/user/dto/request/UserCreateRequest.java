package systemanagercv.example.systemanagercv.user.dto.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateRequest {

    //Định dạng dữ liêu từ form ng dùng nhập từ ngoài vào
    @NotBlank(message = "error.user.username.required") //@NotBlank ép buộc username không được để trống, để khoảng trắng, Nếu vi phạm sẽ thông báo lỗi
    @Size(max = 100, message = "error.user.username.maxLength") // Ép buộc độ dài username không được vượt quá 100 ký tự ,Nếu vi phạm sẽ thông báo lỗi
    private String username;

    @NotBlank(message = "error.user.password.required")
    @Size(min = 6, max = 255, message = "error.user.password.invalidLength")
    private String password;

    @Email(message = "error.user.email.invalid")
    @Size(max = 255, message = "error.user.email.maxLength")
    private String email;

    /*@NotNull: Vì roleId là kiểu số (Long), không phải kiểu chữ (String),
     nên chúng ta không dùng @NotBlank mà phải dùng @NotNull để ép buộc Admin bắt buộc phải chọn 1 quyền (ADMIN, HR, EMPLOYEE...) từ ô Select-Box trên giao diện chứ không được bỏ qua.*/
    @NotNull(message = "error.user.role.required")
    private Long roleId;

    private Boolean enabled = true;

    /*LUỒNG XỬ LÝ:
    * [Giao diện Form tạo User] ➔ (Bấm nút Lưu)
       ↓
    [UserCreateRequest DTO] ➔ Check dữ liệu (@NotBlank, @Size, @Email)
           ├──> Nếu SAI: Dừng lại, trả lỗi hiển thị ra Form HTML.
           └──> Nếu ĐÚNG: Chuyển tiếp cục dữ liệu sạch này vào Controller.
           ↓
    [UserController] ➔ Nhận DTO sạch ➔ Chuyển giao cho Service.
           ↓
    [UserService] ➔ Lấy mật khẩu trong DTO đi băm mã hóa BCrypt ➔ Đổ dữ liệu sang Entity ➔ Gọi Repository lưu vào DB!
    */
}
