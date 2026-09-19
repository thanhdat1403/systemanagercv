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
public class CVEducationResponse {

    private Long id;

    private String schoolName;

    private String major;

    private String degree;

    private LocalDate startDate;

    private LocalDate endDate;

    private String description;

    private Integer sortOrder;
}