package systemanagercv.example.systemanagercv.cv.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import systemanagercv.example.systemanagercv.cv.dto.request.CVCertificateRequest;
import systemanagercv.example.systemanagercv.cv.dto.request.CVEducationRequest;
import systemanagercv.example.systemanagercv.cv.dto.request.CVExperienceRequest;
import systemanagercv.example.systemanagercv.cv.dto.request.CVLanguageRequest;
import systemanagercv.example.systemanagercv.cv.dto.request.CVProfileRequest;
import systemanagercv.example.systemanagercv.cv.dto.request.CVProjectRequest;
import systemanagercv.example.systemanagercv.cv.dto.request.CVSkillRequest;
import systemanagercv.example.systemanagercv.cv.dto.response.CVCertificateResponse;
import systemanagercv.example.systemanagercv.cv.dto.response.CVEducationResponse;
import systemanagercv.example.systemanagercv.cv.dto.response.CVExperienceResponse;
import systemanagercv.example.systemanagercv.cv.dto.response.CVLanguageResponse;
import systemanagercv.example.systemanagercv.cv.dto.response.CVListResponse;
import systemanagercv.example.systemanagercv.cv.dto.response.CVProfileResponse;
import systemanagercv.example.systemanagercv.cv.dto.response.CVProjectResponse;
import systemanagercv.example.systemanagercv.cv.dto.response.CVSkillResponse;
import systemanagercv.example.systemanagercv.cv.dto.response.CVVersionResponse;
import systemanagercv.example.systemanagercv.cv.entity.CVCertificate;
import systemanagercv.example.systemanagercv.cv.entity.CVEducation;
import systemanagercv.example.systemanagercv.cv.entity.CVExperience;
import systemanagercv.example.systemanagercv.cv.entity.CVLanguage;
import systemanagercv.example.systemanagercv.cv.entity.CVProfile;
import systemanagercv.example.systemanagercv.cv.entity.CVProject;
import systemanagercv.example.systemanagercv.cv.entity.CVSkill;
import systemanagercv.example.systemanagercv.cv.projection.CVListProjection;
import systemanagercv.example.systemanagercv.cv.projection.CVVersionProjection;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR // mục đích là nếu sau này sửa DTO nhưng quên mapping, Maven sẽ báo lỗi
)
public interface CVMapper {

    // =====================================================
    // PROJECTION -> RESPONSE
    // =====================================================

    CVListResponse toListResponse(CVListProjection projection);

    CVVersionResponse toVersionResponse(CVVersionProjection projection);

    // =====================================================
    // ENTITY -> RESPONSE
    // =====================================================

    CVProfileResponse toProfileResponse(CVProfile entity);

    CVSkillResponse toSkillResponse(CVSkill entity);

    CVEducationResponse toEducationResponse(CVEducation entity);

    CVExperienceResponse toExperienceResponse(CVExperience entity);

    CVProjectResponse toProjectResponse(CVProject entity);

    CVCertificateResponse toCertificateResponse(CVCertificate entity);

    CVLanguageResponse toLanguageResponse(CVLanguage entity);

    // =====================================================
    // REQUEST -> ENTITY
    // =====================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "cvVersion", ignore = true)
    CVProfile toEntity(CVProfileRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "cvVersion", ignore = true)
    CVSkill toEntity(CVSkillRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "cvVersion", ignore = true)
    CVEducation toEntity(CVEducationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "cvVersion", ignore = true)
    CVExperience toEntity(CVExperienceRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "cvVersion", ignore = true)
    CVProject toEntity(CVProjectRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "cvVersion", ignore = true)
    CVCertificate toEntity(CVCertificateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "cvVersion", ignore = true)
    CVLanguage toEntity(CVLanguageRequest request);
}
/*
* Repository → lấy dữ liệu
Mapper     → chuyển Entity → DTO
Service    → điều phối nghiệp vụ + tổng hợp dữ liệu
Controller → nhận request / trả response*/