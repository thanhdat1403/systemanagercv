package systemanagercv.example.systemanagercv.employee.dto.request;

import lombok.Getter;
import lombok.Setter;
import systemanagercv.example.systemanagercv.employee.enums.EmployeePosition;
import systemanagercv.example.systemanagercv.employee.enums.EmployeeStatus;

@Getter
@Setter
public class EmployeeSearchRequest {

    private String keyword;

    private Long departmentId;

    private EmployeePosition position;

    private EmployeeStatus status;

    private Integer page = 0;

    private Integer size = 10;
}