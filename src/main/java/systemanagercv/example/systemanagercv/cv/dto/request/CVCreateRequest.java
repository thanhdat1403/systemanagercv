package systemanagercv.example.systemanagercv.cv.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVCreateRequest {

    @NotNull(message = "{cv.employeeId.required}")
    private Long employeeId;

    @Valid
    private CVProfileRequest profile;

    @Valid
    @Builder.Default
    private List<CVSkillRequest> skills = List.of();

    @Valid
    @Builder.Default
    private List<CVEducationRequest> educations = List.of();

    @Valid
    @Builder.Default
    private List<CVExperienceRequest> experiences = List.of();

    @Valid
    @Builder.Default
    private List<CVProjectRequest> projects = List.of();

    @Valid
    @Builder.Default
    private List<CVCertificateRequest> certificates = List.of();

    @Valid
    @Builder.Default
    private List<CVLanguageRequest> languages = List.of();
}