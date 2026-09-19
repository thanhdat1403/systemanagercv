package systemanagercv.example.systemanagercv.cv.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVProjectRequest {

    @NotBlank(message = "{cv.project.name.required}")
    @Size(max = 255, message = "{cv.project.name.max}")
    private String projectName;

    @Size(max = 255, message = "{cv.project.role.max}")
    private String role;

    private LocalDate startDate;

    private LocalDate endDate;

    private String description;

    private String technologies;

    @Min(value = 1, message = "{cv.project.teamSize.min}")
    private Integer teamSize;

    private String responsibilities;

    @Min(value = 0, message = "{cv.sortOrder.min}")
    private Integer sortOrder = 0;
}