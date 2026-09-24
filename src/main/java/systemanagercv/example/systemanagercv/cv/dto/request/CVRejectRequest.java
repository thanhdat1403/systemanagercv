package systemanagercv.example.systemanagercv.cv.dto.request;

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
public class CVRejectRequest {

    @NotBlank(message = "{cv.rejectionReason.required}")
    @Size(
            max = 2000,
            message = "{cv.rejectionReason.max}"
    )
    private String rejectionReason;
}