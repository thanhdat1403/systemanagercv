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
public class CVEducationRequest {

    @NotBlank(message = "{cv.education.schoolName.required}")
    @Size(max = 255, message = "{cv.education.schoolName.max}")
    private String schoolName;

    @Size(max = 255, message = "{cv.education.major.max}")
    private String major;

    @Size(max = 255, message = "{cv.education.degree.max}")
    private String degree;

    private LocalDate startDate;

    private LocalDate endDate;

    private String description;

    private Integer sortOrder = 0;
}