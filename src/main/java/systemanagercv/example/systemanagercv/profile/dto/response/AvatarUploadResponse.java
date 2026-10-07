package systemanagercv.example.systemanagercv.profile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import systemanagercv.example.systemanagercv.profile.enums.AvatarChangeRequestStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvatarUploadResponse {

    private Long requestId;

    private AvatarChangeRequestStatus status;
}