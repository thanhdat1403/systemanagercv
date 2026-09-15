package systemanagercv.example.systemanagercv.common.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import systemanagercv.example.systemanagercv.auth.jwt.JwtAuthenticationFilter;
import systemanagercv.example.systemanagercv.common.enums.RoleName;
import systemanagercv.example.systemanagercv.security.CustomUserDetailsService;

@Configuration
@EnableWebSecurity // Annotion này giúp để kích hoạt tính năng bảo mật web (Web Security) trên toàn bộ ứng dụng

/*Annotation này dùng để kích hoạt tính năng phân quyền trực tiếp trên từng hàm (Method) trong code của bạn.
* Tác dụng: Thay vì phải cấu hình phân quyền theo URL một cách thô sơ,
*  bạn có thể nhảy thẳng vào các hàm trong Controller hoặc Service và gắn nhãn phân quyền chi tiết cho hàm đó bằng các từ khóa như @PreAuthorize, @PostAuthorize, @Secured*/
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

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

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                // JWT authentication nên k sử dụng CSRF token
                .csrf(csrf -> csrf.disable())

                .authenticationProvider(authenticationProvider())

                // JWT => Stateless
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/login")
                        .permitAll()

                        .requestMatchers("/api/v1/auth/login")
                        .permitAll()

                        .requestMatchers(
                                "/assets/**",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        )
                        .permitAll()

                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**"
                        )
                        .permitAll()

                        // ==============================
                        // API AUTHORIZATION
                        // ==============================

                        .requestMatchers("/api/v1/users/**")
                        .hasRole(RoleName.ADMIN.name())

                        .requestMatchers("/api/v1/departments/**")
                        .hasRole(RoleName.ADMIN.name())

                        // =========================================================================
                        // LUẬT 1: QUY ĐỊNH CHO HÀNH ĐỘNG XEM (HÀM ĐỌC DỮ LIỆU - GET)
                        // =========================================================================
                        .requestMatchers(HttpMethod.GET, "/api/v1/employees/**") // Bất kỳ ai gọi lệnh GET đến đường dẫn nhân viên (Xem danh sách, xem chi tiết)
                        .hasAnyRole(
                                RoleName.ADMIN.name(),     // Chấp nhận thẻ quyền ADMIN (Quản trị viên)
                                RoleName.HR.name(),        // HOẶC chấp nhận thẻ quyền HR (Nhân sự)
                                RoleName.TECH_LEAD.name(),  // HOẶC chấp nhận thẻ quyền TECH_LEAD (Trưởng nhóm kỹ thuật)
                                RoleName.EMPLOYEE.name()
                        ) // -> Bốn quyền này có quyền xem thông tin nhân viên. Các quyền khác (như EMPLOYEE thường) sẽ bị chặn cửa đuổi về.

                        // =========================================================================
                        // LUẬT 2: QUY ĐỊNH CHO HÀNH ĐỘNG THÊM MỚI (HÀM GHI DỮ LIỆU - POST)
                        // =========================================================================
                        .requestMatchers(HttpMethod.POST, "/api/v1/employees/**") // Bất kỳ ai gọi lệnh POST đến đường dẫn nhân viên (Tính năng Tạo mới nhân viên)
                        .hasAnyRole(
                                RoleName.ADMIN.name(),     // Chỉ chấp nhận ADMIN
                                RoleName.HR.name(),         // HOẶC HR
                                RoleName.TECH_LEAD.name()   // HOẶC TECH_LEAD
                        ) // -> Chỉ có Quản trị viên và phòng Nhân sự mới được phép tạo hồ sơ nhân viên mới. TECH_LEAD lúc này cũng bị chặn.

                        // =========================================================================
                        // LUẬT 3: QUY ĐỊNH CHO HÀNH ĐỘNG CHỈNH SỬA (HÀM CẬP NHẬT - PUT)
                        // =========================================================================
                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/employees",
                                "/api/v1/employees/**") // Bất kỳ ai gọi lệnh PUT đến đường dẫn nhân viên (Tính năng Sửa thông tin nhân viên)
                        .hasAnyRole(
                                RoleName.ADMIN.name(),     // Chỉ chấp nhận ADMIN
                                RoleName.HR.name(),        // Hoặc HR
                                RoleName.TECH_LEAD.name(), // Hoặc TECH_LEAD
                                RoleName.EMPLOYEE.name()    // Hoặc EMPLOYEE
                        ) // -> Chỉ có ADMIN hoặc HR mới được phép thay đổi, chỉnh sửa hồ sơ nhân viên.

                        // =========================================================================
                        // LUẬT 4: QUY ĐỊNH CHO HÀNH ĐỘNG XÓA (HÀM TIÊU HỦY DỮ LIỆU - DELETE)
                        // =========================================================================
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/employees/**") // Bất kỳ ai gọi lệnh DELETE đến đường dẫn nhân viên (Tính năng Xóa nhân viên)
                        .hasRole(RoleName.ADMIN.name()) // Chỉ chấp nhận một mình quyền ADMIN duy nhất (hasRole dùng cho 1 quyền duy nhất)
                        // -> Lệnh xóa cực kỳ nguy hiểm, nên hệ thống thắt chặt tối đa: Chỉ có sếp lớn nhất (ADMIN) mới được quyền xóa nhân viên. HR hay TECH_LEAD đều bị cấm hoàn toàn.



                        // ==============================
                        // THYMELEAF PAGE AUTHORIZATION
                        // ==============================

                        .requestMatchers("/admin/**")
                        .hasRole(RoleName.ADMIN.name())

                        .requestMatchers("/hr/**")
                        .hasRole(RoleName.HR.name())

                        .requestMatchers("/tech-lead/**")
                        .hasRole(RoleName.TECH_LEAD.name())

                        .requestMatchers("/employee/**")
                        .hasRole(RoleName.EMPLOYEE.name())

                        .anyRequest()
                        .authenticated()
                )

                // ==============================
                // JWT FILTER
                // ==============================
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}