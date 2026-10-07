package systemanagercv.example.systemanagercv.department.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DepartmentOptionResponse {

    private Long id;

    private String code;

    private String name;
}