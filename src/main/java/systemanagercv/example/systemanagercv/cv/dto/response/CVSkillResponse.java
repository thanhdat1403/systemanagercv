package systemanagercv.example.systemanagercv.cv.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVSkillResponse {

    private Long id;

    private String skillName;

    private String skillLevel;

    private String description;

    private Integer sortOrder;
}