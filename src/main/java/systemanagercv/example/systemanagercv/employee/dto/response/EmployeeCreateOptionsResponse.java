package systemanagercv.example.systemanagercv.employee.dto.response;

import lombok.Builder;
import lombok.Getter;
import systemanagercv.example.systemanagercv.department.dto.response.DepartmentOptionResponse;
import systemanagercv.example.systemanagercv.user.dto.response.UserSelectResponse;

import java.util.List;

@Getter
@Builder
public class EmployeeCreateOptionsResponse {

    private List<UserSelectResponse> users;

    private List<DepartmentOptionResponse> departments;
}
