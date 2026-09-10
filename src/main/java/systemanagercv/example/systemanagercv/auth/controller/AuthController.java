package systemanagercv.example.systemanagercv.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import systemanagercv.example.systemanagercv.auth.dto.request.LoginRequest;
import systemanagercv.example.systemanagercv.auth.dto.response.LoginResponse;
import systemanagercv.example.systemanagercv.auth.service.AuthService;
import systemanagercv.example.systemanagercv.common.response.ApiResponse;

@RestController // Đánh dấu class này là API Controller (trả về dữ liệu dạng JSON)
@RequestMapping("/api/v1/auth") // Cấu hình đường dẫn gốc cho tất cả các API trong class này
@RequiredArgsConstructor // Tự động tạo Constructor để nhúng (Inject) AuthService vào
public class AuthController {

    // Đặt tên cho cái "bánh quy" Cookie sẽ lưu ở trình duyệt là "accessToken"
    private static final String ACCESS_TOKEN_COOKIE = "accessToken";

    // Khai báo tầng Service để xử lý logic kiểm tra tài khoản mật khẩu
    private final AuthService authService;

    // Định nghĩa API đăng nhập: đón nhận Request dạng POST tới đường dẫn /api/v1/auth/login
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request, // Nhận và tự động kiểm tra tính hợp lệ dữ liệu (username, password) gửi lên từ Client
            HttpServletResponse response// Biến này dùng để can thiệp và ghi dữ liệu (như Cookie) trả về cho trình duyệt
    ){
        //1. Gọi xuống tầng Service để kiểm tra đăng nhập
        // Nếu đúng, Service trả về object chứa token và thời gian hết hạn (expiesIn)
        LoginResponse loginResponse  =
                authService.login(request);

        // 2. Tạo một chiếc Cookie mới tên là "accessToken" chứa giá trị là chuỗi Token vừa lấy được
        Cookie accessTokenCookie = new Cookie(
                ACCESS_TOKEN_COOKIE,
                loginResponse.getAccessToken());

        // 3. Cấu hình bảo mật cho Cookie:
        // Chỉ cho phép môi trường HTTP truyền nhận, cấm JavaScript (FE) đọc chuỗi này -> Chống hacker hack token qua mã độc XSS
        accessTokenCookie.setHttpOnly(true);

        // Đang để false, nghĩa là chạy ở HTTP thường được (Nếu chạy thực tế sản phẩm (Production), nên đổi thành true để bắt buộc dùng HTTPS bảo mật)
        accessTokenCookie.setSecure(false);

        // Đường dẫn mà Cookie này có hiệu lực. Để "/" nghĩa là toàn bộ trang web/API đều dùng được Cookie này
        accessTokenCookie.setPath("/");

        // Đặt thời gian sống cho Cookie (Trình duyệt tính bằng Giây, trong khi loginResponse tính bằng Mili-giây nên phải chia cho 1000)
        accessTokenCookie.setMaxAge(
                (int) (loginResponse.getExpiresIn() / 1000)
        );

        // 4. Đính kèm chiếc Cookie vừa cấu hình ở trên vào phản hồi (Response) để gửi về cho trình duyệt của User
        response.addCookie(accessTokenCookie);


        // 5. Trả về phản hồi thành công dạng JSON chứa thông tin LoginResponse cho Frontend hiển thị giao diện
        return ApiResponse.success(loginResponse);

    }
}
