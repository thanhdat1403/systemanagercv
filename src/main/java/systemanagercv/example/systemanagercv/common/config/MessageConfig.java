package systemanagercv.example.systemanagercv.common.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/*@Configuration: Báo cho Spring Boot biết đây là một file bản thiết kế linh kiện hệ thống.
 Khi ứng dụng khởi chạy, Spring sẽ quét file này đầu tiên để nạp các cấu hình cốt lõi.*/
@Configuration
public class MessageConfig {

    @Bean //Tạo ra 1 đối tượng (Bean) tên là MessageSource và nạp vào bộ nhớ của Spring để các lớp khác (như MessageService) có thể gọi dùng chung
    public MessageSource messageSource() {

        //Khởi tạo một công cụ đọc file ngôn ngữ có tính năng "Reloadable(có thể nạp lại)"
        //Tính năng này giúp hệ thống tự động cập nhật chữ mới nếu bn sửa file .properties ngôn ngữ mà k cần phải tắt đi khởi động lại dự án
        ReloadableResourceBundleMessageSource messageSource =
                new ReloadableResourceBundleMessageSource();

        //Chỉ đường dẫn cho Spring biết các file ngôn ngữ đang nằm ở đâu
        //"classpath:messages/messages" nghĩa là các file dịch phải nằm trong thư mục: src/main/resources/messages/
        // Và tên các file phải bắt đầu bằng chữ "messages" (Ví dụ: messages.properties, messages_vi.properties, messages_en.properties)
        messageSource.setBasename("classpath:messages/messages");

        //Cấu hình định dạng phông chữ là UTF-8
        //Điều này cực kỳ quan trọng đối với tiếng việt, giúp các câu thông báo có dấu (như "Thành công", "Lỗi") hiển thị chuẩn xác, không bị lỗi font thành các ký tự lạ ô vuông hay hỏi chấm.
        messageSource.setDefaultEncoding("UTF-8");

        //Trả về đối tượng cấu hình hoàn chỉnh cho Spring quản lý
        return messageSource;
    }
}
