package systemanagercv.example.systemanagercv.cv.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import systemanagercv.example.systemanagercv.cv.enums.CvStatus;
import systemanagercv.example.systemanagercv.cv.enums.CvVersionStatus;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

// Đây là chi tiết của CV
public class CVDetailResponse {

    private Long id;

    private Long employeeId;

    private String employeeCode;

    private String employeeName;

    private String employeeEmail;

    private String departmentCode;

    private String departmentName;

    private CvStatus status;

    private Long currentVersionId;

    private String currentVersion;

    private CvVersionStatus currentVersionStatus;

    private Long workflowVersionId;

    private String workflowVersion;

    private CvVersionStatus workflowVersionStatus;

    private String rejectionReason;

    private CVProfileResponse profile;

    @Builder.Default
    private List<CVSkillResponse> skills = List.of();

    @Builder.Default
    private List<CVEducationResponse> educations = List.of();

    @Builder.Default
    private List<CVExperienceResponse> experiences = List.of();

    @Builder.Default
    private List<CVProjectResponse> projects = List.of();

    @Builder.Default
    private List<CVCertificateResponse> certificates = List.of();

    @Builder.Default
    private List<CVLanguageResponse> languages = List.of();
}