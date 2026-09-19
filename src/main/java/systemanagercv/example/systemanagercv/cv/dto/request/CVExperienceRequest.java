package systemanagercv.example.systemanagercv.cv.dto.request;

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
public class CVExperienceRequest {

    @NotBlank(message = "{cv.experience.companyName.required}")
    @Size(max = 255, message = "{cv.experience.companyName.max}")
    private String companyName;

    @Size(max = 255, message = "{cv.experience.position.max}")
    private String position;

    private LocalDate startDate;

    private LocalDate endDate;

    private String description;

    private Integer sortOrder = 0;
}