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
public class CVCertificateResponse {

    private Long id;

    private String certificateName;

    private String organization;

    private LocalDate issueDate;

    private LocalDate expiryDate;

    private String credentialId;

    private String description;

    private Integer sortOrder;
}