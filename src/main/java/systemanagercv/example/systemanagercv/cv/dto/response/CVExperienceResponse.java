package systemanagercv.example.systemanagercv.cv.dto.response;

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
public class CVExperienceResponse {

    private Long id;

    private String companyName;

    private String position;

    private LocalDate startDate;

    private LocalDate endDate;

    private String description;

    private Integer sortOrder;
}