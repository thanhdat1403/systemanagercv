package systemanagercv.example.systemanagercv.common.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import systemanagercv.example.systemanagercv.common.message.MessageService;
import systemanagercv.example.systemanagercv.common.response.ApiResponse;

@RestControllerAdvice // Đánh dấu đây là bộ xử lý ngoại lệ tập trung cho toàn bộ các Controller trong dự án
@RequiredArgsConstructor //tự động viết hộ một hàm khởi tạo (Constructor) chứa tất cả các biến được khai báo với từ khóa final
public class GlobalExceptionHandler {

    //Tiêm MessageService vào để lát nữa dùng dịch ngôn ngữ cho mã lỗi
    private final MessageService messageService;

    //Đánh dấu hàm này chuyên dùng để "bắt" riêng loại lỗi BusinessException khi nó xảy ra trong hệ thống
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException exception //Biến 'exception' chưa thông tin của lỗi vừa bị ném ra
    ) {
        //Bước 1: Lấy mã lỗi từ exception (VD: "user.notfound") đưa vào MessageService để bốc ra câu thông báo đa ngôn ngữ tương ứng
        String message = messageService.getMessage(
                exception.getCode()
        );

        //Bước 2: Khởi tạo khuôn mẫu ApiResponse để trả về. Vì là lỗi nên ô dữ liệu thực sự (data) sẽ để là 'null'
        //Kiểu dữ liệu truyền vào là Void nghĩa là chiếc hộp ApiResponse này không chứa dữ liệu bên trong.
        ApiResponse<Void> response = new ApiResponse<>(
                exception.getCode(), //Đưa mã lỗi vào trường code
                message,             // Đưa câu thông báo đã dịch vào trường message
                null                 // Trường data bằng null
        );

        //Bước 3: Trả về cho Frontend với mã trạng thái HTTP là 404 (BAD_REQUEST -Yêu cầu không hợp lệ) kèm theo phần ruột là đối tượng response trên
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}


/*Chốt lại: Khi lỗi BusinessException bị ném ra ở bất kỳ đâu trong hệ thống (Tầng Service, tầng Repository...), đoạn code này đóng vai trò như một tấm lưới tự động
* hứng lấy quả bóng lỗi đó
*
* Thay vì để hệ thống trả về một màn hình trắng xóa, hoặc một lỗi Java loằng ngoằng làm sập ứng dụng, thì hàm handlerBusinessException sẽ:
* 1.Bắt lấy mã lỗi (code) từ BusinessException.
* 2.Nhờ MessageService dịch mã lỗi đó sang ngôn ngữ phù hợp (Tiếng Việt hoặc Tiếng Anh).
* 3.Bọc mọi thứ vào cái khuôn ApiResponse một cách lịch sự với trạng thái lỗi 400 Bad Request.
* 4.Gửi trả về cho Frontend một cấu trúc JSON sạch sẽ, rõ ràng để hiển thị cho người dùng.*/
