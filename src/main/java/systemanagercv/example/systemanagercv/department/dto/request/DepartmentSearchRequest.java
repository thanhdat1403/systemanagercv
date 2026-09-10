package systemanagercv.example.systemanagercv.department.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import systemanagercv.example.systemanagercv.department.enums.DepartmentStatus;

@Getter
@Setter
public class DepartmentSearchRequest {

    private String keyword;

    private DepartmentStatus status;

    @Min(value = 0, message = "error.page.invalid")
    private Integer page = 0;

    @Min(value = 1, message = "error.page.size.invalid")
    @Max(value = 100, message = "error.page.size.max")
    private Integer size = 10;

    private String sortBy = "id";

    private String sortDirection = "ASC";
}