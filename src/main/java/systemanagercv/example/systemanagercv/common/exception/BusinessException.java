package systemanagercv.example.systemanagercv.common.exception;

import lombok.Getter;

@Getter // Tự động tạo hàm getCode() để lớp khác có thể lấy mã lỗi ra dùng
public class BusinessException extends RuntimeException { // Kế thừa RuntimeException để biến lớp này thành một lỗi hệ thống có thể chủ động "ném" ra (throw)

    // Thuộc tính lưu trữ mã lỗi nghiệp vụ định sẵn (Ví dụ: "user.notfound", "email.exists")
    private final String code;

    /**
     * Hàm khởi tạo (Constructor) - bắt buộc phải truyền vào một mã lỗi (code) khi tạo ra lỗi này
     */
    public BusinessException(String code) {
        super(code); // Truyền mã code này lên lớp cha (RuntimeException) để làm thông điệp báo lỗi mặc định
        this.code = code; // Lưu mã code vào thuộc tính của riêng lớp BusinessException để sau này dùng đối chiếu ngôn ngữ
    }
}
/*Chốt lại tác dụng của lớp này: giống như "1 chiếc thẻ đỏ tự chế" dùng riêng cho các lỗi logic trong dự án
* (Ví dụ: gõ sai mật khẩu, trùng eamil, tài khoản bị khóa). Khi code chạy đến đoạn vi phạm quy định, thì chỉ cần ném lỗi
* này ra: throw new BusinessException("user.notfound");*/

/*Ví dụ: Trước đây trong UserServiceImpl:
* .orElseThrow(() ->
        new RuntimeException("Không tìm thấy User!")
); (Không nên)
* Sau khi có BusinessException: .orElseThrow(() ->
        new BusinessException("error.user.notFound")
); thì Service chỉ cần biết mã lỗi: error.user.notFound
* Sau đó GlobalExceptionHandler sẽ xử lý lấy message tương ứng:
* vi → Không tìm thấy người dùng
en → User not found*/