package systemanagercv.example.systemanagercv.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // Đánh dấu đây là một linh kiện (Bean) được Spring tự động nạp vào bộ nhớ để sử dụng
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter { // OncePerRequestFilter: Spring Security sẽ đảm bảo Filter này được xử lý một lần cho mỗi request

    // Khai báo hằng số vị trí tìm vé: Tìm trong nhãn "Authorization" ở phần Header của gói tin gửi lên
    private static final String AUTHORIZATION_HEADER = "Authorization";

    // Khai báo định dạng tiền tố của vé: Vé JWT chuẩn luôn bắt đầu bằng chữ "Bearer" kèm khoảng trắng
    private static final String BEARER_PREFIX = "Bearer ";

    private static final String ACCESS_TOKEN_COOKIE = "accessToken";

    private final JwtService jwtService; // Công cụ đọc vé và kiểm tra chữ ký đóng dấu mà ta đã làm

    private final UserDetailsService userDetailsService; // Công cụ đi tìm thông tin User trong Database của Spring Security

    /**
     * Hàm lõi xử lý việc chặn Request để kiểm tra vé thông hành
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        /*
         * =========================================================
         * BƯỚC 1: TÌM JWT
         * =========================================================
         *
         * Ưu tiên tìm JWT trong Authorization Header.
         *
         * Ví dụ:
         *
         * Authorization: Bearer eyJ...
         *
         * Nếu không có Header thì tìm tiếp trong Cookie.
         *
         * Cookie:
         * accessToken=eyJ...
         */

        String jwt = extractTokenFromRequest(request);

        /*
         * Nếu request không có JWT thì bỏ qua filter.
         *
         * Sau đó Spring Security sẽ tự quyết định:
         *
         * - Endpoint permitAll()
         *      -> cho phép
         *
         * - Endpoint authenticated()/hasRole()
         *      -> từ chối nếu chưa đăng nhập
         */
        if (jwt == null || jwt.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        /*
         * =========================================================
         * BƯỚC 2: LẤY USERNAME TỪ JWT
         * =========================================================
         */

        final String username;

        try {
            username = jwtService.extractUsername(jwt);
        } catch (Exception exception) {

            /*
             * JWT không hợp lệ:
             * - Sai chữ ký
             * - Token bị sửa
             * - Token sai format
             * - Token hết hạn
             *
             * Không xác thực user.
             */
            filterChain.doFilter(request, response);
            return;
        }

        /*
         * =========================================================
         * BƯỚC 3: KIỂM TRA SECURITY CONTEXT
         * =========================================================
         */
        /*
         * Chỉ thực hiện kiểm tra đóng dấu nếu:
         * 1. Đọc được username hợp lệ ở Bước 3
         * 2. SecurityContextHolder... == null (Nghĩa là yêu cầu này chưa từng được ai đóng dấu xác thực trước đó trong luồng này)
         */
        if (username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            // Chui xuống Database bốc thông tin tài khoản đầy đủ của người này lên dựa vào username trên vé
            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(username);

            /*
             * =====================================================
             * BƯỚC 4: KIỂM TRA JWT
             * =====================================================
             */
            /*
             * Kiểm tra xem tấm vé JWT gửi lên có trùng khớp thông tin với User vừa lấy dưới DB lên hay không
             * (Hàm isTokenValid sẽ kiểm tra: Đúng tên không? Vé còn hạn sử dụng không?)
             */
            if (jwtService.isTokenValid(jwt, userDetails)) {

                /*
                 * Tạo Authentication.
                 *
                 * Authorities được lấy trực tiếp từ UserDetails.
                 *
                 * Với admin:
                 *
                 * ROLE_ADMIN
                 */
                // Đúc ra một chiếc thẻ chứng nhận đăng nhập thành công chính chủ (UsernamePasswordAuthenticationToken)
                // đính kèm sẵn danh sách Quyền (Authorities/Roles) của người đó lấy từ DB lên.
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null, // Mật khẩu không cần truyền vào nữa vì vé đã chứng minh hợp lệ rồi
                                userDetails.getAuthorities()
                        );

                // Bổ sung thêm các thông tin phụ của trình duyệt gửi lên (Địa chỉ IP, Session ID,..) vào thẻ chứng nhận
                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                /*
                 * =================================================
                 * BƯỚC 5: ĐƯA USER VÀO SECURITY CONTEXT
                 * =================================================
                 */
                /*
                 * ĐÚT THẺ CHỨNG NHẬN VÀO TÚI ÁO (SecurityContext)
                 * Đây là dòng code QUAN TRỌNG NHẤT. Khi đút thẻ này vào SecurityContext, Spring Security sẽ chính thức công nhận:
                 * "Yêu cầu này là hợp lệ, đang được thực hiện bởi user X, và user X này có những quyền hạn Y, Z."
                 * Đi qua dòng này, người dùng sẽ chính thức được phép truy cập vào các hàm lấy dữ liệu nhạy cảm ở phía sau.
                 */
                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            }
        }


        // =========================================================
        // Bước 6: Hoàn thành nhiệm vụ gác cổng, cho phép Request đi tiếp
        // =========================================================
        // Chuyển tiếp yêu cầu và gói dữ liệu sang cho các Filter bảo mật tiếp theo hoặc vào thẳng Controller xử lý dữ liệu
        filterChain.doFilter(request, response);
    }

    /**
     * Chức năng: Tự động tìm kiếm và lấy vé JWT ra từ gói yêu cầu (Request) của client.
     * Quy tắc ưu tiên:
     *   - Ưu tiên số 1: Kiểm tra trên thanh tiêu đề (Authorization Header)
     *   - Ưu tiên số 2: Kiểm tra trong bộ lưu trữ Cookie của trình duyệt (Cookie accessToken)
     */
    private String extractTokenFromRequest(
            HttpServletRequest request
    ) {
        /*
         * =========================================================
         * CÁCH 1: Authorization Header (THỬ TÌM VÉ TRÊN THANH TIÊU ĐỀ)
         * =========================================================
         */
        // Thò tay vào Header lấy chuỗi dữ liệu ở mục "Authorization" ra xem
        String authHeader =
                request.getHeader(AUTHORIZATION_HEADER);

        // Nếu trên tiêu đề có dữ liệu, VÀ chuỗi chữ đó bắt đầu bằng định dạng "Bearer " chuẩn quy định
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {

            // Lập tức cắt bỏ 7 ký tự đầu (chữ "Bearer") để lấy nguyên chuỗi mã token sạch trả về luôn
            return authHeader.substring(BEARER_PREFIX.length());
        }

        /*
         * =========================================================
         * CÁCH 2: NẾU CÁCH 1 KHÔNG CÓ -> THỬ TÌM VÉ TRONG HÒM THƯ COOKIE (Browser Cookies)
         * =========================================================
         */
        // Lấy ra toàn bộ danh sách các chiếc bánh Cookie mà trình duyệt gửi kèm lên hệ thống
        Cookie[]  cookies = request.getCookies();

        // Nếu danh sách Cookie này k bị trống (người dùng có lưu cookie trên máy)
        if (cookies != null) {

            // Bật vòng lặp duyệt qua từng chiếc bánh Cookie một từ đầu đến cuối danh sách
            for (Cookie cookie : cookies) {

                // Kiểm tra xem tên của chiếc bánh Cookie hiện tại đang cầm trên tay
                // có trùng khớp 100% với tên hằng số cấu hình sẵn (Ví dụ: "accessToken") hay không
                if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {

                    // Nếu tìm đúng chiếc bánh tên là "accessToken", lập tức bốc phần ruột dữ liệu (mã token) bên trong trả về
                    return cookie.getValue();
                }
            }
        }
        /*
         * KẾT CỤC: Nếu đã tìm cả trên Header lẫn lùng sục trong Cookie mà vẫn không thấy tấm vé nào
         * -> Trả về giá trị 'null' báo hiệu: Người này hoàn toàn không mang theo vé thông hành.
         */
        return null;
    }
}
/*POST /api/v1/auth/login
        │
        ▼
   AuthController
        │
        ▼
   AuthServiceImpl
        │
        ▼
AuthenticationManager
        │
        ▼
 kiểm tra username/password
        │
        ▼
    JwtService
        │
        ▼
   tạo JWT Token
        │
        ▼
      Client*/