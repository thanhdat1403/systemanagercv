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
public class CVProjectResponse {

    private Long id;

    private String projectName;

    private String role;

    private LocalDate startDate;

    private LocalDate endDate;

    private String description;

    private String technologies;

    private Integer teamSize;

    private String responsibilities;

    private Integer sortOrder;
}