package systemanagercv.example.systemanagercv.department.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import systemanagercv.example.systemanagercv.department.enums.DepartmentStatus;

@Getter
@Setter
public class DepartmentRequest {

    //Dữ liệu từ html xuống

    @NotBlank(message = "Mã phòng ban không được bỏ trống")
    @Size(max = 50, message = "Mã phòng ban không được vượt quá 50 ký tự")
    private String code;

    @NotBlank(message = "Tên phòng ban không được bỏ trống")
    @Size(max = 255, message = "Tên phòng ban không được vượt quá 255 ký tự")
    private String name;

    private String description; //không cần thêm @Size cho description vì DB đang dùng TEXT.

    @NotNull(message = "Trạng thái không được để trống")
    private DepartmentStatus status;
}
