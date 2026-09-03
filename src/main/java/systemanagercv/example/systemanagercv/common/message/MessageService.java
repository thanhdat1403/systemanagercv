package systemanagercv.example.systemanagercv.common.message;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service // Đánh dấu đây là 1 Bean thuộc tầng Service để Spring Boot quản lý và cho phép tiêm (inject) vào chỗ khác
@RequiredArgsConstructor // Tự động sinh hàm khởi tạo cho các biến final ở dưới
public class MessageService {

    //MessageSource là công cụ có sẵn của Spring để đọc nội dung từ các file cấu hình ngôn ngữ (.properties)
    private final MessageSource messageSource;

    /**
     * Hàm lấy câu thông báo dựa vào mã code (Ví dụ truyền vào "user.notfound")
     */
    public String getMessage(String code) {
        return messageSource.getMessage(
                code,       //Mã định danh của câu thông báo trong file .properties
                null,       // Mảng các tham số truyền thêm vào câu lệnh (ở đây không dùng nên để null)
                LocaleContextHolder.getLocale() //Tự động lấy ra ngôn ngữ hiện tại của người dùng (Ví dụ: Tiếng Việt 'vi' hoặc Tiếng Anh 'en')
        );
    }
}
/*Cấu trúc xử lý sẽ là:
* Client
   │
   │ Accept-Language: vi
   ↓
Controller
   ↓
Service / ExceptionHandler
   ↓
MessageService
   ↓
messages_vi.properties
   ↓
"Không tìm thấy người dùng"*/
