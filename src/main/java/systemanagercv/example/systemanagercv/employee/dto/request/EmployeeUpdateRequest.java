package systemanagercv.example.systemanagercv.employee.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import systemanagercv.example.systemanagercv.employee.enums.EmployeePosition;
import systemanagercv.example.systemanagercv.employee.enums.EmployeeStatus;

import java.time.LocalDate;

@Getter
@Setter
public class EmployeeUpdateRequest {

    @NotNull(message = "ID tài khoản không được để trống")
    private Long userId;

    @NotBlank(message = "Mã nhân viên không được để trống")
    @Size(max = 50, message = "Mã nhân viên không được vượt quá 50 ký tự")
    private String employeeCode;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 255, message = "Họ tên không được vượt quá 255 ký tự")
    private String fullName;

    @Email(message = "Email không đúng định dạng")
    @Size(max = 255, message = "Email không được vượt quá 255 ký tự")
    private String email;

    @Size(max = 30, message = "Số điện thoại không được vượt quá 30 ký tự")
    private String phone;

    @NotNull(message = "Phòng ban không được để trống")
    private Long departmentId;

    private EmployeePosition position;

    @Size(max = 150, message = "Chức danh không được vượt quá 150 ký tự")
    private String jobTitle;

    private LocalDate joinDate;

    @NotNull(message = "Trạng thái không được để trống")
    private EmployeeStatus status;
}