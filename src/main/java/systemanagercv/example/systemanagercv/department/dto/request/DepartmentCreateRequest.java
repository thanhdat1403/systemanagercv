package systemanagercv.example.systemanagercv.department.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import systemanagercv.example.systemanagercv.department.enums.DepartmentStatus;

@Getter
@Setter
public class DepartmentCreateRequest {

    @NotBlank(message = "error.department.code.required")
    @Size(max = 50, message = "error.department.code.maxLength")
    private String code;

    @NotBlank(message = "error.department.name.required")
    @Size(max = 255, message = "error.department.name.maxLength")
    private String name;

    private String description;

    @NotNull(message = "error.department.status.required")
    private DepartmentStatus status = DepartmentStatus.ACTIVE;
}