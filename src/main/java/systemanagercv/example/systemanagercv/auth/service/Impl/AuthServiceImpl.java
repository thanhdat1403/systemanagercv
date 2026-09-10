package systemanagercv.example.systemanagercv.auth.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import systemanagercv.example.systemanagercv.auth.dto.request.LoginRequest;
import systemanagercv.example.systemanagercv.auth.dto.response.LoginResponse;
import systemanagercv.example.systemanagercv.auth.jwt.JwtService;
import systemanagercv.example.systemanagercv.auth.service.AuthService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class AuthServiceImpl implements AuthService {

    // AuthenticationManager: Công cụ cốt lõi của Spring Security chịu trách nhiệm xác minh tài khoản/ mật khẩu
    private final AuthenticationManager authenticationManager;

    // JwtService: Trung tâm quản lý vé thông hành mà bạn vừa làm từ JwtService
    private final JwtService jwtService;

    /**
     * Hàm xử lý Đăng nhập (Login) hệ thống
     */
    @Override
    public LoginResponse login(LoginRequest request) {

        /*
         * --------------------------------------------------------
         * Bước 1: Tiến hành Xác thực tài khoản và mật khẩu (Authentication)
         * --------------------------------------------------------
         * - Tạo ra một phong bì xác thực thô (UsernamePasswordAuthenticationToken) chứa username và password người dùng vừa gõ.
         * - Ném phong bì này vào máy authenticationManager.authenticate(...).
         * - Cách vận hành: Spring Security sẽ tự động chui xuống Database, tìm user, bốc mật khẩu đã băm ra để đối chiếu.
         *   + Nếu SAI mật khẩu hoặc tài khoản không tồn tại: Hàm này lập tức ném ra lỗi (ví dụ BadCredentialsException)
         *     làm dừng code lại ngay, không cho chạy xuống dưới.
         *   + Nếu ĐÚNG mật khẩu: Trả về một đối tượng 'authentication' đã được đóng dấu chứng nhận hợp lệ thành công.
         */
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        /*
         * --------------------------------------------------------
         * Bước 2: Bốc tách thông tin User hợp lệ sau khi xác thực thành công
         * --------------------------------------------------------
         * Lấy thông tin chi tiết của người dùng vừa đăng nhập thành công ra (Ép kiểu về UserDetails)
         */
        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        /*
         * --------------------------------------------------------
         * Bước 3: Đúc vé thông hành (Generate JWT Token)
         * --------------------------------------------------------
         * Nhờ JwtService đúc ra chuỗi vé thông hành dài loằng ngoằng dựa vào thông tin của User đó
         */
        String accessToken =
                jwtService.generateToken(userDetails);

        /*
         * --------------------------------------------------------
         * Bước 4: Lấy danh sách Quyền (Roles) của User để phản hồi về giao diện
         * --------------------------------------------------------
         * Duyệt qua danh sách quyền, cắt chữ "ROLE_" đi (Ví dụ: "ROLE_ADMIN" -> "ADMIN") để Frontend dễ xử lý
         */
        List<String> roles =
                userDetails.getAuthorities()
                        .stream()
                        .map(authority -> authority
                                .getAuthority()
                                .replace("ROLE_", ""))
                        .toList();
        /*
         * --------------------------------------------------------
         * Bước 5: Đóng gói dữ liệu kết quả trả về (Build LoginResponse)
         * --------------------------------------------------------
         * Trả về một khối đối tượng chứa đầy đủ: Vé thông hành, loại vé (Bearer), hạn sử dụng (3600000ms = 1 tiếng),
         * tên đăng nhập và danh sách quyền để Frontend cấu hình hiển thị giao diện.
         */
        return LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                //.expiresIn(3600000L) // Cấu hình thời gian hết hạn của vé gửi về cho Frontend biết
                /*Vì đã tạo public long getExpiration() {
                return expiration; để lấy thời gian hết hạn của access token nên ta k
                hard-code 3600000L nữa
                }*/
                .expiresIn(jwtService.getExpiration())
                .username(userDetails.getUsername())
                .roles(roles)
                .build();
    }
}
