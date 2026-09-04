package systemanagercv.example.systemanagercv.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

@Configuration // Đánh dấu đây là lớp cấu hình hệ thống, được kích hoạt ngay khi ứng dụng khởi động
//Để MessageService thực sự hoạt động theo ngôn ngữ request, chúng ra nên cấu hình Locale Resolver
public class LocaleConfig {

    @Bean // Tạo ra một đối tượng localeResolver và nạp vào bộ nhớ Spring để hệ thống tự động sử dụng khi có người gọi API
    public AcceptHeaderLocaleResolver localeResolver() {
        //Khởi tạo công cụ nhận diện ngôn ngữ dựa vào "Accpet-Language" nằm trong Header của Request gửi lên
        //Giải thích: Khi trình duyệt (Chrome,Safari,...) hoặc Frontend gọi API, họ sẽ tự động đính kèm một thông tin
        //ở Header tên là "Accpet-Language" Ví dụ: "vi" nếu máy dùng tiếng Việt, hoặc "en" nếu máy dùng tiếng Anh).
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();

        //Đặt ngôn ngữ mặc định cho hệ thống là tiếng Anh (Locale.ENGLISH).
        //Tác dụng: Nếu người dùng truy cập từ một thiết bị lạ mà hệ thống không thể nhận diện ngôn ngữ của họ,
        //hoặc hệ thống không hỗ trợ tếng của họ (ví dụ tiếng Pháp, tiếng Đức), hệ thông sẽ tự động chọn tiếng Anh để hiển thị.
        resolver.setDefaultLocale(Locale.ENGLISH);

        // có thể đổi sang tiếng Việt
//        Locale vietnamese = new Locale("vi", "VN");
//        resolver.setDefaultLocale(vietnamese);
        return resolver;
    }
}
