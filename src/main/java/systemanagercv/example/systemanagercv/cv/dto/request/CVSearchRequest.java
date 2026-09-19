package systemanagercv.example.systemanagercv.cv.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import systemanagercv.example.systemanagercv.cv.enums.CvStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVSearchRequest {

    private String keyword;

    private Long departmentId;

    private CvStatus status;

    @Builder.Default
    @Min(value = 0, message = "{pagination.page.min}")
    private Integer page = 0;

    @Builder.Default
    @Min(value = 1, message = "{pagination.size.min}")
    @Max(value = 100, message = "{pagination.size.max}")
    private Integer size = 10;

    @Builder.Default
    private String sortBy = "id";

    @Builder.Default
    private String sortDirection = "DESC";
}