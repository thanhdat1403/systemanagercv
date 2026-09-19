package systemanagercv.example.systemanagercv.cv.dto.response;

import lombok.*;
import systemanagercv.example.systemanagercv.cv.enums.CvVersionStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVVersionResponse {

    private Long id;

    private Long employeeCvId;

    private String version;

    private CvVersionStatus status;

    private Boolean current;

}
/* DTO khớp hoàn toàn với:
* CVVersionProjection
        ↓
CVVersionResponse*/