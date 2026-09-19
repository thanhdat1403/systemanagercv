package systemanagercv.example.systemanagercv.cv.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVProfileResponse {

    private Long id;

    private String headline;

    private String address;

    private String avatarUrl;

    private String careerObjective;
}
