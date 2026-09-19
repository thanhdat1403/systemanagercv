package systemanagercv.example.systemanagercv.cv.dto.request;

import jakarta.validation.Valid;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
/*CVUpdateRequest k phải là CV Update Request của nghiệp vụ yêu cầu nhân viên cập nhật CV
* Tên này dễ gây nhầm:
* Trong giai đoạn CV Core, CVUpdateRequest chỉ là: dữ liệu cập nhật CV hiện tại*/
public class CVUpdateRequest {

    @Valid //Kích hoạt bộ kiểm duyệt dữ liệu (Hibernate Validator)
    private CVProfileRequest profile;

    @Valid
    @Builder.Default //Giữ cố định giá trị mặc định của biến khi dùng chuỗi khởi tạo @Builder.
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
