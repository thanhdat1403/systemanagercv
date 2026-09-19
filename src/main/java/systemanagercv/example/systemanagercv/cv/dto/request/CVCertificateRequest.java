package systemanagercv.example.systemanagercv.cv.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class CVCertificateRequest {

    @NotBlank(message = "{cv.certificate.name.required}")
    @Size(max = 255, message = "{cv.certificate.name.max}")
    private String certificateName;

    @Size(max = 255, message = "{cv.certificate.organization.max}")
    private String organization;

    private LocalDate issueDate;

    private LocalDate expiryDate;

    @Size(max = 255, message = "{cv.certificate.credentialId.max}")
    private String credentialId;

    private String description;

    private Integer sortOrder = 0;
}