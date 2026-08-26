package systemanagercv.example.systemanagercv.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import systemanagercv.example.systemanagercv.service.CustomUserDetailsService;

/*@Configuration:đóng vai trò là một "Bản thiết kế trung tâm" hoặc một "Sổ tay hướng dẫn" cho Spring Boot Factory (IoC Container) biết cách lắp ráp các linh kiện phức tạp cho hệ thống
 * Nếu @Component báo cho Spring biết một Class đơn lẻ, thì @Configuration báo cho Spring biết đây là một Nơi tập hợp, khai báo và cấu hình nhiều Bean cùng một lúc thông qua các hàm có gắn @Bean*/
@Configuration // 1. Báo cho Spring: "Tôi là file cấu hình hệ thống đây!"
@EnableWebSecurity // Kích hoạt tính năng bảo mật web nâng cao
public class SecurityConfig {
    @Autowired
    // TÁC DỤNG: Tiêm file CustomUserDetailsService
    // Giúp file cấu hình này biết đường tìm xuống Database thông qua Service
    private CustomUserDetailsService customUserDetailsService;

    // PASSWORD ENCODER
    /**
     * 1. CẤU HÌNH BỘ MÃ HÓA MẬT KHẨU
     * Mã hóa mật khẩu thô thành chuỗi BCrypt trước khi lưu vào MariaDB.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(customUserDetailsService);

        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    /**
     * 🌟 CẤU HÌNH BỘ XÁC THỰC TOÀN CỤC (AUTHENTICATION MANAGER)
     * Đây là đoạn code quan trọng nhất bắt buộc Spring Boot phải hủy bỏ tài khoản ảo mặc định
     * và quay sang sử dụng Database của bạn để kiểm tra tài khoản.
     */
    // 1. Khai báo nhân viên kiểm duyệt dữ liệu từ Database
    @Bean
    public AuthenticationManager authenticationManager(
            org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration authenticationConfiguration) throws Exception {

        // 1. Đọc bộ quản lý xác thực từ cấu hình hệ thống
        AuthenticationManager authManager = authenticationConfiguration.getAuthenticationManager();

        // 🌟 ÉP IN LOG: Ghi đè để ép hệ thống bắt buộc phải chạy qua và in log kiểm tra
        return new AuthenticationManager() {
            @Override
            public org.springframework.security.core.Authentication authenticate(org.springframework.security.core.Authentication authentication)
                    throws org.springframework.security.core.AuthenticationException {

                System.out.println("==================== HỆ THỐNG KIỂM TRA MẬT KHẨU ====================");
                System.out.println("👉 Tài khoản đăng nhập gửi lên: " + authentication.getName());
                System.out.println("👉 Độ dài mật khẩu thô gửi lên: " + authentication.getCredentials().toString().length() + " ký tự");
                System.out.println("====================================================================");

                return authManager.authenticate(authentication);
            }
        };
    }

    /**
     * 2. BỘ LỌC PHÂN QUYỀN VÀ KHAI THÔNG SWAGGER / POSTMAN
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                //Bước A: TẮT LỚP BẢO VỆ CSRF
                //Bắt buộc phải tắt thì Postman mới gửi được lệnh POST, PUT, DELETE lên hệ thống được
                .csrf(csrf -> csrf.disable())
                .authenticationProvider(authenticationProvider())

                //Bước B: Bật HTTP BASIC
                //Cho phép Postman gửi tài khoản/mật khẩu qua tab Authorization > Basic Auth
                .httpBasic(Customizer.withDefaults())

                //Bước C: PHÂN CHIA RANH GIỚI ĐƯỜNG DẪN URL
                .authorizeHttpRequests(auth -> auth
                    //Quy tắc 1: KHAI THÔNG SWAGGER UI (Mở cửa tự do cho toàn bộ file tài liệu API)
                    //Thếu các dòng này thì Swagger sẽ bị lỗi 401/403 khong thể hiển thị được giao diện test
                                .requestMatchers(
                                        "/v3/api-docs/**",    // Dữ liệu JSON gốc của Swagger
                                        "/swagger-ui/**",     // Giao diện Swagger UI xem trên trình duyệt
                                        "/swagger-ui.html",   // Đường dẫn phụ vào trang Swagger
                                        "/swagger-resources/**",
                                        "/webjars/**"
                                ).permitAll()
                        //Quy tắc 2: Mở cửa tự do cho trang chủ và tài nguyên tĩnh
                        .requestMatchers(
                                "/assets/**",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()
                        //Quy tắc 3: Bảo vệ nghiêm ngặt khu vực quản trị viên (Admin)
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/hr/**").hasRole("HR") // THÊM DÒNG NÀY: Chỉ ai có ROLE_HR mới được vào khu vực này
                        .requestMatchers("/tech-lead/**").hasRole("TECH_LEAD")
                        .requestMatchers("/employee/**").hasRole("EMPLOYEE")

                        /*Sự khác nhau giữa hasRole vs hasAuthority:
                         * hasRole("ADMIN): tự động thêm tiền tố ROLE_ vào chuỗi trước khi truyền và tìm đúng giá trị ROLE_ADMIN trong DB
                         * hasAuthority("ADMIN"): giữ nguyên 100% chuỗi truyền vào mà không thêm bớt và tìm chính xác chữ ADMIN trong DB*/

                        //Quy tắc 4: Tất cả các Request khác bắt buộc phải đăng nhập (Áp dụng khi test trên postman)
                        .anyRequest().authenticated()
                )
                //Bước D: CẤU HÌNH FORM ĐĂNG NHẬP CHO GIAO DIỆN WEB
                .formLogin(login -> login
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username") //là dữ liệu truyền từ ngoài vào (Method Parameter)
                        .passwordParameter("password")

                        // ĐOẠN ĐIỀU HƯỚNG THÔNG MINH THEO VAI TRÒ
                        .successHandler((request, response, authentication) -> {
                            //1.Lấy ra danh sách các quyền (thẻ từ) của tài khoản vừa đăng nhập thành công
                            //var tự động nhận diện kiểu dữ liệu của biến
                            //Java tự nhìn thấy hàm getAuthorities() trả về một collection nên nó tự biến 'authorities' thành kiểu đó luôn
                            var authorities = authentication.getAuthorities();

                            //2. Chuyển danh sách quyền thành danh sách chữ Spring để kiểm tra cho dễ
                            var roles = authorities.stream()
                                    .map(grantedAuthority -> grantedAuthority.getAuthority())
                                    .collect(java.util.stream.Collectors.toSet());

                            // CHÈN THÊM ĐOẠN LOG QUYỀN HẠN NÀY VÀO ĐÂY ĐỂ KIỂM TRA:
                            System.out.println("==================== KIỂM TRA QUYỀN HẠN ====================");
                            System.out.println("-> Danh sách quyền Spring Security nhận được: " + roles);
                            System.out.println("-> Có khớp chữ 'ROLE_ADMIN' không? : " + roles.contains("ROLE_ADMIN"));
                            System.out.println("=============================================================");

                            //3. RẼ NHÁNH ĐƯỜNG DẪN DỰA TRÊN QUYỀN HẠN
                            //Lưu ý: CustomUserDetails của mình có nối thêm chữ "ROLE_" ở trước tên quyền nên ta kiểm tra chuỗi có chữ ROLE_
                            if (roles.contains("ROLE_ADMIN")) {
                                response.sendRedirect("/admin/index");
                            } else if (roles.contains("ROLE_HR")) {
                                response.sendRedirect("/hr/index");
                            }  else if (roles.contains("ROLE_TECH_LEAD")) {
                                response.sendRedirect("/tech-lead/index");
                            } else if (roles.contains("ROLE_EMPLOYEE")) {
                                response.sendRedirect("/employee/index");
                            } else  {
                                response.sendRedirect("/index-cv");
                            }
                        }).permitAll()

                        //Hiển thị log (không dùng thì xóa)
                        .failureHandler((request, response, exception) -> {

                            System.out.println("========== LOGIN FAILED ==========");

                            System.out.println(
                                    "Lý do: " + exception.getClass().getSimpleName()
                            );

                            System.out.println(
                                    "Message: " + exception.getMessage()
                            );

                            System.out.println("==================================");

                            response.sendRedirect("/login?error");
                        })

                )
                // BƯỚC E: CẤU HÌNH ĐĂNG XUẤT (LOGOUT)
                .logout(logout -> logout
                        .logoutUrl("/logout")               // Đường dẫn kích hoạt hành động logout (Gửi POST/GET lên đây)
                        .logoutSuccessUrl("/login")  // Đường dẫn hệ thống sẽ đá user về sau khi logout thành công
                        .invalidateHttpSession(true)        // Xóa sạch Session trên Server
                        .deleteCookies("JSESSIONID")        // Xóa Cookie định danh trên trình duyệt của User
                        .permitAll()                        // Cho phép tất cả mọi người truy cập đường dẫn logout này
                );

        return http.build();
    }

}
