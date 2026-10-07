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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


/**
 * ============================================================
 * JWT AUTHENTICATION FILTER
 * ============================================================
 *
 * Nhiệm vụ:
 *
 * 1. Lấy JWT từ Authorization Header hoặc Cookie.
 * 2. Đọc username từ JWT.
 * 3. Tìm User trong Database.
 * 4. Kiểm tra JWT hợp lệ.
 * 5. Nếu hợp lệ -> tạo Authentication.
 * 6. Đưa Authentication vào SecurityContext.
 *
 * Đặc biệt:
 *
 * Nếu JWT cũ vẫn chứa username đã bị đổi trong Database
 * thì KHÔNG được để UsernameNotFoundException biến thành 500.
 *
 * Ví dụ:
 *
 * JWT:
 *     admin
 *
 * Database:
 *     thanhdat
 *
 * Khi đó request được coi như chưa xác thực.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {


    /* =========================================================
     * CONSTANT
     * ========================================================= */

    private static final String AUTHORIZATION_HEADER =
            "Authorization";

    private static final String BEARER_PREFIX =
            "Bearer ";

    private static final String ACCESS_TOKEN_COOKIE =
            "accessToken";


    /* =========================================================
     * DEPENDENCY
     * ========================================================= */

    private final JwtService jwtService;

    private final UserDetailsService userDetailsService;


    /* =========================================================
     * FILTER
     * ========================================================= */

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {


        /* =====================================================
         * 1. LẤY JWT
         * ===================================================== */

        String jwt =
                extractTokenFromRequest(
                        request
                );


        /*
         * Không có JWT.
         *
         * Cho request đi tiếp.
         *
         * Spring Security sẽ quyết định:
         *
         * - permitAll() -> cho phép
         * - authenticated()/hasRole() -> từ chối
         */
        if (
                jwt == null
                        ||
                        jwt.isBlank()
        ) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        /* =====================================================
         * 2. LẤY USERNAME TỪ JWT
         * ===================================================== */

        final String username;


        try {

            username =
                    jwtService.extractUsername(
                            jwt
                    );

        } catch (Exception exception) {

            /*
             * JWT không hợp lệ:
             *
             * - Sai format
             * - Sai chữ ký
             * - Hết hạn
             * - Token bị thay đổi
             *
             * Không xác thực user.
             */
            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        /* =====================================================
         * 3. KIỂM TRA SECURITY CONTEXT
         * ===================================================== */

        if (
                username != null
                        &&
                        SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                == null
        ) {

            try {

                /*
                 * =================================================
                 * 3.1. TÌM USER
                 * =================================================
                 */

                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(
                                        username
                                );


                /*
                 * =================================================
                 * 3.2. KIỂM TRA JWT
                 * =================================================
                 */

                if (
                        jwtService.isTokenValid(
                                jwt,
                                userDetails
                        )
                ) {

                    /*
                     * =================================================
                     * 3.3. TẠO AUTHENTICATION
                     * =================================================
                     */

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );


                    /*
                     * Thêm thông tin request:
                     *
                     * IP
                     * Session
                     * ...
                     */
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(
                                            request
                                    )
                    );


                    /*
                     * =================================================
                     * 3.4. GHI VÀO SECURITY CONTEXT
                     * =================================================
                     */

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(
                                    authentication
                            );
                }


            } catch (
                    UsernameNotFoundException exception
            ) {

                /*
                 * =================================================
                 * USER KHÔNG CÒN TỒN TẠI
                 * =================================================
                 *
                 * Trường hợp quan trọng:
                 *
                 * JWT cũ:
                 *     admin
                 *
                 * Database:
                 *     thanhdat
                 *
                 * Hoặc User đã bị soft delete.
                 *
                 * Không được ném 500.
                 *
                 * Chỉ đơn giản coi request này
                 * là chưa được xác thực.
                 */

                SecurityContextHolder
                        .clearContext();


            } catch (Exception exception) {

                /*
                 * Không để lỗi Authentication
                 * làm ứng dụng trả 500 ngoài ý muốn.
                 *
                 * Request tiếp tục đi qua.
                 * Endpoint protected sẽ tự xử lý
                 * theo SecurityConfig.
                 */

                SecurityContextHolder
                        .clearContext();

            }
        }


        /* =====================================================
         * 4. REQUEST ĐI TIẾP
         * ===================================================== */

        filterChain.doFilter(
                request,
                response
        );
    }


    /* =========================================================
     * EXTRACT JWT
     * ========================================================= */

    private String extractTokenFromRequest(
            HttpServletRequest request
    ) {


        /* =====================================================
         * CÁCH 1:
         * Authorization Header
         * ===================================================== */

        String authHeader =
                request.getHeader(
                        AUTHORIZATION_HEADER
                );


        if (
                authHeader != null
                        &&
                        authHeader.startsWith(
                                BEARER_PREFIX
                        )
        ) {

            return authHeader.substring(
                    BEARER_PREFIX.length()
            );
        }


        /* =====================================================
         * CÁCH 2:
         * COOKIE
         * ===================================================== */

        Cookie[] cookies =
                request.getCookies();


        if (cookies != null) {

            for (
                    Cookie cookie :
                    cookies
            ) {

                if (
                        ACCESS_TOKEN_COOKIE
                                .equals(
                                        cookie.getName()
                                )
                ) {

                    return cookie.getValue();
                }
            }
        }


        /*
         * Không tìm thấy JWT.
         */
        return null;
    }

}