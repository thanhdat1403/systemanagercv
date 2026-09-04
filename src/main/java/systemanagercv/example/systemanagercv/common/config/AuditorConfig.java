package systemanagercv.example.systemanagercv.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import systemanagercv.example.systemanagercv.common.auditor.SpringSecurityAuditorAware;

/*@Configuration: Báo cho Spring Boot biết đây là một file bản thiết kế linh kiện hệ thống.
 Khi ứng dụng khởi chạy, Spring sẽ quét file này đầu tiên để nạp các cấu hình cốt lõi.*/
@Configuration
/*@EnableJpaAuditing: Đây là nút "Bật công tắc nguồn".
 Nếu thiếu nhãn này, toàn bộ các tính năng thông minh như @CreatedDate, @CreatedBy, @LastModifiedDate trong file BaseEntity sẽ hoàn toàn bị "tê liệt" (bị bỏ qua và trả về giá trị trống null)*/
@EnableJpaAuditing
public class AuditorConfig {
    /*@Bean: Báo cho Spring Boot: "Hãy chạy hàm dưới đây,
     lấy đối tượng trả về cất vào trong kho tổng (IoC Container) để làm linh kiện dùng chung cho toàn dự án".*/
    @Bean
    public AuditorAware<String> auditorAware() {
        return new SpringSecurityAuditorAware();
    }
}
/*LUỒNG XỬ LÝ:
* LOGIN
  ↓
SecurityConfig
  ↓
CustomUserDetailsService
  ↓
UserRepository
  ↓
User
  ↓
CustomUserDetails
  ↓
Authentication
  ↓
SecurityContextHolder
  ↓
SpringSecurityAuditorAware
  ↓
BaseEntity*/
