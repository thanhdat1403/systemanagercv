package systemanagercv.example.systemanagercv.common.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import systemanagercv.example.systemanagercv.security.CustomUserDetailsService;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Password Encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication Provider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(customUserDetailsService);

        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    /**
     * Security Filter Chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

                // ==============================
                // CSRF
                // ==============================
                .csrf(csrf -> csrf.disable())

                // ==============================
                // Authentication Provider
                // ==============================
                .authenticationProvider(authenticationProvider())

                // ==============================
                // AUTHORIZATION
                // ==============================
                .authorizeHttpRequests(auth -> auth

                        // Login page
                        .requestMatchers("/login").permitAll()

                        // Static resources
                        .requestMatchers(
                                "/assets/**",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()

                        // Swagger
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // ADMIN
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        // HR
                        .requestMatchers("/hr/**")
                        .hasRole("HR")

                        // TECH LEAD
                        .requestMatchers("/tech-lead/**")
                        .hasRole("TECH_LEAD")

                        // EMPLOYEE
                        .requestMatchers("/employee/**")
                        .hasRole("EMPLOYEE")

                        // Các URL còn lại
                        .anyRequest().authenticated()
                )

                // ==============================
                // FORM LOGIN
                // ==============================
                .formLogin(login -> login

                        // Trang login
                        .loginPage("/login")

                        // Spring Security nhận POST /login
                        .loginProcessingUrl("/login")

                        // Tên input username
                        .usernameParameter("username")

                        // Tên input password
                        .passwordParameter("password")

                        // Login thành công
                        .successHandler((request, response, authentication) -> {

                            var roles = authentication
                                    .getAuthorities()
                                    .stream()
                                    .map(grantedAuthority ->
                                            grantedAuthority.getAuthority())
                                    .collect(
                                            java.util.stream.Collectors.toSet()
                                    );

                            System.out.println(
                                    "========== LOGIN SUCCESS =========="
                            );

                            System.out.println(
                                    "Username: "
                                            + authentication.getName()
                            );

                            System.out.println(
                                    "Roles: " + roles
                            );

                            System.out.println(
                                    "==================================="
                            );

                            // ADMIN
                            if (roles.contains("ROLE_ADMIN")) {

                                response.sendRedirect(
                                        "/admin/index"
                                );

                            }

                            // HR
                            else if (roles.contains("ROLE_HR")) {

                                response.sendRedirect(
                                        "/hr/index"
                                );

                            }

                            // TECH LEAD
                            else if (roles.contains("ROLE_TECH_LEAD")) {

                                response.sendRedirect(
                                        "/tech-lead/index"
                                );

                            }

                            // EMPLOYEE
                            else if (roles.contains("ROLE_EMPLOYEE")) {

                                response.sendRedirect(
                                        "/employee/index"
                                );

                            }

                            // Không có role hợp lệ
                            else {

                                response.sendRedirect(
                                        "/login?error"
                                );
                            }
                        })

                        // Login thất bại
                        .failureHandler((request, response, exception) -> {

                            System.out.println(
                                    "========== LOGIN FAILED =========="
                            );

                            System.out.println(
                                    "Username: "
                                            + request.getParameter(
                                            "username")
                            );

                            System.out.println(
                                    "Exception: "
                                            + exception
                                            .getClass()
                                            .getSimpleName()
                            );

                            System.out.println(
                                    "Message: "
                                            + exception.getMessage()
                            );

                            System.out.println(
                                    "=================================="
                            );

                            response.sendRedirect(
                                    "/login?error"
                            );
                        })

                        .permitAll()
                )

                // ==============================
                // LOGOUT
                // ==============================
                .logout(logout -> logout

                        .logoutUrl("/logout")

                        .logoutSuccessUrl("/login")

                        .invalidateHttpSession(true)

                        .deleteCookies("JSESSIONID")

                        .permitAll()
                );

        return http.build();
    }
}