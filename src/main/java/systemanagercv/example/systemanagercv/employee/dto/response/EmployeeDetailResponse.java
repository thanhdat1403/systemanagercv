package systemanagercv.example.systemanagercv.employee.dto.response;

import lombok.Builder;
import lombok.Getter;
import systemanagercv.example.systemanagercv.employee.enums.EmployeePosition;
import systemanagercv.example.systemanagercv.employee.enums.EmployeeStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class EmployeeDetailResponse {

    private Long id;

    private Long userId;
    private String username;
    /*Ở đây cố tính dùng userEmail để tránh nhầm:
    * userEmail
    → users.email

    email
        → employees.email*/
    private String userEmail;

    private String employeeCode;
    private String fullName;
    private String email;
    private String phone;

    private EmployeePosition position;
    private String positionDescription;

    private String jobTitle;

    private LocalDate joinDate;

    private EmployeeStatus status;
    private String statusDescription;

    private Long departmentId;
    private String departmentCode;
    private String departmentName;

    private LocalDateTime createdDate;
    private String createdBy;

    private LocalDateTime updatedDate;
    private String updatedBy;
}

/*Tại sao lại phải cần cả EmployeeResponse và EmployeeDetailsResponse?
* Vì đây là cách thiết kế phổ biến:
* - EmployeeResponse: dùng cho danh sách Employee (GET /api/v1/employees):{
* trả ra chuỗi JSON có dữ liệu từ các trường trên
}
*
* - EmployeeDetailResponse: dùng cho chi tiết của 1 Employee (GET /api/v1/employees/1)
* có thể có thêm: hasEmployee để biết được liên kế*/