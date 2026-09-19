package systemanagercv.example.systemanagercv.cv.dto.request;

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
public class CVProfileRequest {

    @Size(max = 255, message = "{cv.profile.headline.max}")
    private String headline;

    @Size(max = 500, message = "{cv.profile.address.max}")
    private String address;

    @Size(max = 1000, message = "{cv.profile.avatarUrl.max}")
    private String avatarUrl;

    private String careerObjective;
}