package systemanagercv.example.systemanagercv.cv.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class CVSkillRequest {

    @NotBlank(message = "{cv.skill.name.required}")
    @Size(max = 255, message = "{cv.skill.name.max}")
    private String skillName;

    @Size(max = 50, message = "{cv.skill.level.max}")
    private String skillLevel;

    private String description;

    @Min(value = 0, message = "{cv.sortOrder.min}")
    private Integer sortOrder = 0;
}