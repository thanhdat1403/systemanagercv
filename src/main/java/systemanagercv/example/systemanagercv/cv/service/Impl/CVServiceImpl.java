package systemanagercv.example.systemanagercv.cv.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import systemanagercv.example.systemanagercv.common.enums.RoleName;
import systemanagercv.example.systemanagercv.common.exception.ResourceNotFoundException;
import systemanagercv.example.systemanagercv.cv.dto.request.*;
import systemanagercv.example.systemanagercv.cv.dto.response.CVDetailResponse;
import systemanagercv.example.systemanagercv.cv.dto.response.CVListResponse;
import systemanagercv.example.systemanagercv.cv.entity.*;
import systemanagercv.example.systemanagercv.cv.enums.CvStatus;
import systemanagercv.example.systemanagercv.cv.enums.CvVersionStatus;
import systemanagercv.example.systemanagercv.cv.mapper.CVMapper;
import systemanagercv.example.systemanagercv.cv.repository.*;
import systemanagercv.example.systemanagercv.cv.service.CVService;
import systemanagercv.example.systemanagercv.employee.authorization.EmployeeAccessScope;
import systemanagercv.example.systemanagercv.employee.authorization.EmployeeAccessType;
import systemanagercv.example.systemanagercv.employee.authorization.EmployeeAuthorizationService;
import systemanagercv.example.systemanagercv.employee.entity.Employee;
import systemanagercv.example.systemanagercv.employee.repository.EmployeeRepository;

import org.springframework.security.access.AccessDeniedException;
import systemanagercv.example.systemanagercv.notification.enums.NotificationType;
import systemanagercv.example.systemanagercv.notification.service.NotificationService;
import systemanagercv.example.systemanagercv.user.entity.User;
import systemanagercv.example.systemanagercv.user.repository.UserRepository;
import systemanagercv.example.systemanagercv.user.service.UserService;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
public class CVServiceImpl implements CVService {

    private static final String INITIAL_VERSION = "V1.0";
    /*Tại sao cần 4 dependency này:
    * EmployeeCVRepository dùng để:
    * CV tồn tại?
        Tạo CV
        Tìm CV
        Tìm danh sách CV
     * EmployeeRepository dùng để: Employee employee = employeeRepository.findById(request.getEmployeeId())
     * CVMapper dùng đúng theo convention (K map trong Controller): Request → Entity
                                           Entity/Projection → Response
     * EmployeeAuthorizationService (k tạo lại logic): ADMIN, HR, TECH_LEAD, EMPLOYEE mà tái sử dụng employeeAuthorizationService.getCurrentUserScope()*/
    private final EmployeeCVRepository employeeCVRepository;
    private final CVVersionRepository cvVersionRepository;
    private final CVProfileRepository cvProfileRepository;
    private final CVSkillRepository cvSkillRepository;
    private final CVEducationRepository cvEducationRepository;
    private final CVExperienceRepository cvExperienceRepository;
    private final CVProjectRepository cvProjectRepository;
    private final CVCertificateRepository cvCertificateRepository;
    private final CVLanguageRepository cvLanguageRepository;

    private final UserService userService;
    private final EmployeeRepository employeeRepository;
    private final CVMapper cvMapper;
    private final EmployeeAuthorizationService employeeAuthorizationService;


    private final UserRepository userRepository;

    private final NotificationService notificationService;

    // Tạo CV
    @Override
    public CVDetailResponse create(CVCreateRequest cvCreateRequest) {

        // 1. Tìm Employee mà CV này thuộc về
        Employee employee = employeeRepository
                .findEmployeeDetailById(cvCreateRequest.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên không tồn tại"));

        // 2. Kiểm tra quyền tạo CV
        if (!employeeAuthorizationService.canCreateCV(employee)) {
            throw new AccessDeniedException(
                    "Bạn không có quyền tạo CV cho nhân viên này"
            );
        }

        // 3. Kiểm tra nhân viên đã có CV hay chưa
        if (employeeCVRepository.existsByEmployeeIdAndDeletedFalse(employee.getId())) {
            throw new IllegalStateException(
                    "Nhân viên đã có CV"
            );
        }

        // 4. Tạo EmployeeCV
        EmployeeCV employeeCV = new EmployeeCV();

        employeeCV.setEmployee(employee);
        employeeCV.setStatus(CvStatus.UPDATED);

        // 5. Lưu EmployeeCV
        employeeCV = employeeCVRepository.save(employeeCV);

        // 6. Tạo version đầu tiên
        /*currentVersion
        │
        ├── employeeCV → employeeCV
        ├── version    → "V1.0"
        ├── status     → OFFICIAL
        └── isCurrent  → true*/
        CVVersion currentVersion = new CVVersion();

        currentVersion.setEmployeeCV(employeeCV);
        currentVersion.setVersion(INITIAL_VERSION);
        currentVersion.setStatus(CvVersionStatus.OFFICIAL);
        currentVersion.setIsCurrent(true);

        /*Tại sao lại phải save 2 lần ?
        * employee_cvs
            current_version_id ──────> cv_versions.id

          cv_versions
                employee_cv_id ──────────> employee_cvs.id
        * Có 1 vòng tham chiếu:
        * EmployeeCV ────────> CVVersion
         ▲                  │
         └──────────────────┘*/

        // 7. Lưu version
        CVVersion savedVersion = cvVersionRepository.save(currentVersion);

        // 8. Đặt version hiện tại cho EmployeeCV
        employeeCV.setCurrentVersion(savedVersion);

        employeeCVRepository.save(employeeCV);

        // 9. Tạo CV Profile
        if (cvCreateRequest.getProfile() != null) {

            CVProfile profile = cvMapper.toEntity(
                    cvCreateRequest.getProfile()
            );

            profile.setCvVersion(savedVersion);
            cvProfileRepository.save(profile);
        }

        // 10. Tạo Skills
        // Kiểm tra danh sách skill truyền vào: Nếu null thì gán bằng danh sách rỗng (tránh lỗi NullPointerException), ngược lại thì lấy danh sách từ request
        List<CVSkillRequest> skillRequests =
                cvCreateRequest.getSkills() == null
                    ? List.of()
                    : cvCreateRequest.getSkills();

        // Duyệt qua danh sách Request để chuyển đổi dữ liệu và xử lý liên kết
        List<CVSkill> skills = skillRequests.stream()
                .map(cvMapper::toEntity) // Chuyển đổi (Map) từng đối tượng Request thành đối tượng Entity để lưu xuống DB
                .map(skill -> {
                    skill.setCvVersion(savedVersion);
                    return skill;
                })  // Gắn mối quan hệ: Liên kết mỗi Skill với phiên bản CV hiện tại (savedVersion)
                .toList(); // Thu gom tất cả kết quả lại thành 1 danh sách (List)

        // Lưu thành bộ danh sách Kỹ năng (Skills) vào cơ sở dữ liệu cùng một lúc
        cvSkillRepository.saveAll(skills);

        // 11. Tạo Educations
        // Nếu danh sách Educations là null thì sử dụng danh sách rỗng
        List<CVEducationRequest> educationRequests =
                cvCreateRequest.getEducations() == null
                    ? List.of()
                    : cvCreateRequest.getEducations();

        // Request -> Entity -> gắn CVVersion hiện tại
        List<CVEducation> educations = educationRequests.stream()
                .map(cvMapper::toEntity)
                .map(education -> {
                    education.setCvVersion(savedVersion);
                    return education;
                })
                .toList();

        // Lưu toàn bộ Educations
        cvEducationRepository.saveAll(educations);

        // 12. Tạo Experience
        //Nếu danh sách Experiences là null thì sử dụng danh sách rỗng
        List<CVExperienceRequest> experienceRequests =
                cvCreateRequest.getExperiences() == null
                    ? List.of()
                    : cvCreateRequest.getExperiences();

        // Request -> Entity -> gắn CVVersion hiện tại
        List<CVExperience> experiences = experienceRequests.stream()
                .map(cvMapper::toEntity)
                .map(experience -> {
                    experience.setCvVersion(savedVersion);
                    return experience;
                })
                .toList();

        // Lưu toàn bộ Experience
        cvExperienceRepository.saveAll(experiences);

        // 13. Tạo Projects
        // Nếu danh sách Projects là null thì sử dụng danh sách rỗng
        List<CVProjectRequest> projectRequests =
                cvCreateRequest.getProjects() == null
                        ? List.of()
                        : cvCreateRequest.getProjects();

        // Request → Entity → gắn CVVersion hiện tại
        List<CVProject> projects = projectRequests.stream()
                .map(cvMapper::toEntity)
                .map(project -> {
                    project.setCvVersion(savedVersion);
                    return project;
                })
                .toList();

        // Lưu toàn bộ Projects
        cvProjectRepository.saveAll(projects);

        // 14. Tạo Certificates
        // Nếu danh sách Certificates là null thì sử dụng danh sách rỗng
        List<CVCertificateRequest> certificateRequests =
                cvCreateRequest.getCertificates() == null
                        ? List.of()
                        : cvCreateRequest.getCertificates();

        // Request → Entity → gắn CVVersion hiện tại
        List<CVCertificate> certificates = certificateRequests.stream()
                .map(cvMapper::toEntity)
                .map(certificate -> {
                    certificate.setCvVersion(savedVersion);
                    return certificate;
                })
                .toList();

        // Lưu toàn bộ Certificates
        cvCertificateRepository.saveAll(certificates);

        // 15. Tạo Languages
        // Nếu danh sách Languages là null thì sử dụng danh sách rỗng
        List<CVLanguageRequest> languageRequests =
                cvCreateRequest.getLanguages() == null
                        ? List.of()
                        : cvCreateRequest.getLanguages();

        // Request → Entity → gắn CVVersion hiện tại
        List<CVLanguage> languages = languageRequests.stream()
                .map(cvMapper::toEntity)
                .map(language -> {
                    language.setCvVersion(savedVersion);
                    return language;
                })
                .toList();

        // Lưu toàn bộ Languages
        cvLanguageRepository.saveAll(languages);


        // 16. Trả về Detail
        return getDetail(employeeCV.getId());
    }

    // Tìm kiếm + phân trang, sắp xếp
    @Override
    @Transactional(readOnly = true)
    public Page<CVListResponse> search(CVSearchRequest request) {

        // 1. Khởi tạo bộ cấu hình phân trang và sắp xếp (Page, Size, SortBy, SortDirection) từ request gửi lên.
        Pageable pageable = createPageable(request);

        // 2. Lấy ra "Phạm vi quyền hạn" (Scope) của tài khoản đang đăng nhập hiện tại trong hệ thống.
        EmployeeAccessScope accessScope =
                employeeAuthorizationService.getCurrentUserScope();

        // Khởi tạo scope filter.
        // null không đại diện trực tiếp cho ADMIN,
        // mà biểu thị không giới hạn theo department/employee.
        // AccessType.ALL mới là scope có toàn quyền xem.
        Long scopeDepartmentId = null;
        Long scopeEmployeeId  = null;

        // 3. Phân luồng kiểm tra quyền hạn (Bảo mật ở tầng Service):

        // Nếu người đăng nhập có quyền cấp PHÒNG BAN (Ví dụ: Trưởng phòng):
        if (accessScope.getAccessType() == EmployeeAccessType.DEPARTMENT){
            scopeDepartmentId = accessScope.getDepartmentId(); // Gán ID phòng ban của họ vào bộ lọc
        }

        // Nếu người đăng nhập chỉ có quyền CÁ NHÂN (Ví dụ: Nhân viên thường):
        if (accessScope.getAccessType() == EmployeeAccessType.SELF){
            scopeEmployeeId = accessScope.getEmployeeId();
        }

        // 4. Chuẩn hóa và làm sạch từ khóa tìm kiếm (gọt khoảng trắng, biến chuỗi rỗng thành null)
        String keyword = normalizeKeyword(request.getKeyword());

        // 5. Thọc xuống tầng Repository để bắn câu lệnh SQL thông minh xuống Database
        return employeeCVRepository
                .searchActiveForList(
                        keyword,                    // Từ khóa đã làm sạch
                        request.getDepartmentId(),  // ID phòng ban muốn lọc (nếu có)
                        request.getStatus(),        // Trạng thái CV muốn lọc (nếu có)
                        scopeDepartmentId,          // Chốt chặn ID phòng ban theo quyền (nếu có)
                        scopeEmployeeId,            // Chốt chặn ID nhân viên theo quyền (nếu có)
                        pageable                    // Bộ cấu hình phân trang/sắp xếp
                )
                // 6. Sau khi DB trả về danh sách Entity (EmployeeCV), dùng cvMapper để "đóng gói"
                // chuyển đổi (Mapping) chúng sang dạng DTO (CVListResponse) trước khi trả về cho Controller.
                .map(cvMapper::toListResponse);
    }


    // lấy chi tiết một bản CV của nhân viên dựa vào ID
    @Override
    @Transactional(readOnly = true) // Tối ưu hóa hiệu năng DB: Chỉ đọc, không ghi/cập nhật dữ liệu
    public CVDetailResponse getDetail(Long id) {
        // 1. Tìm CV đang hoạt động theo ID, nếu k thấy thì ném lỗi 404
        EmployeeCV employeeCV = employeeCVRepository
                .findActiveById(id)
                .orElseThrow(() ->  new ResourceNotFoundException("CV không tồn tại"));

        // 2. Lấy thông tin nhân viên sở hữu CV này
        Employee employee = employeeCV.getEmployee();

        // 3. Kiểm tra quyền: Nếu người dùng hiện tại k có quyền xem, ném lỗi 403 (AccessDenied)
        if (!employeeAuthorizationService.canView(employee.getId())) {
            throw new AccessDeniedException(
                    "Bạn không có quyền xem CV này"
            );
        }

        // 4. Lấy thông tin phiên bản (version) hiện tại của CV
        CVVersion currentVersion = employeeCV.getCurrentVersion();

        // 5. Khởi tạo đối tượng Response và map các thông tin cơ bản của Nhân viên & CV
        CVDetailResponse response = CVDetailResponse.builder()
                .id(employeeCV.getId())
                .employeeId(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .employeeName(employee.getFullName())
                .employeeEmail(employee.getEmail())
                .departmentCode(employee.getDepartment().getCode())
                .departmentName(employee.getDepartment().getName())
                .status(employeeCV.getStatus())
                .build();

        // 6. Nếu CV chưa có phiên bản nào (mới tạo, trống), trả về ngay thông tin cơ bản trên
        if (currentVersion == null){
            return response;
        }

        // 7. Điền thêm thông tin định danh của phiên bản hiện tại vào Response
        response.setCurrentVersionId(currentVersion.getId());
        response.setCurrentVersion(currentVersion.getVersion());
        response.setCurrentVersionStatus(currentVersion.getStatus());

        // 7.1. Lấy version mới nhất đang trong workflow
        List<CVVersion> versions =
                cvVersionRepository
                        .findAllByEmployeeCVIdAndDeletedFalseOrderByCreatedDateDesc(
                                employeeCV.getId()
                        );

        // 7.2. Version đầu tiên là version mới nhất
        if (!versions.isEmpty()){

            CVVersion latestVersion = versions.get(0);

            // Chỉ xem là workflow version nếu không phải version hiện tại
            if (!latestVersion.getId().equals(currentVersion.getId())){

                response.setWorkflowVersionId(latestVersion.getId());
                response.setWorkflowVersion(latestVersion.getVersion());
                response.setWorkflowVersionStatus(latestVersion.getStatus());
                response.setRejectionReason(latestVersion.getRejectionReason());
            }
        }

        Long versionId = currentVersion.getId();

        // 8. Lấy thông tin hồ sơ cá nhân (Profile) gắn với phiên bản CV (nếu có và chưa bị xóa)
        cvProfileRepository
                .findByCvVersionIdAndDeletedFalse(versionId)
                .map(cvMapper::toProfileResponse)
                .ifPresent(response::setProfile);

        // 9. Lấy danh sách Kỹ năng (Skills) chưa xóa, xếp theo thứ tự hiển thị tăng dần
        response.setSkills(
                cvSkillRepository
                        .findAllByCvVersionIdAndDeletedFalseOrderBySortOrderAsc(versionId)
                        .stream()
                        .map(cvMapper::toSkillResponse)
                        .toList()
        );

        // 10. Lấy danh sách Học vấn (Educations) chưa xóa, xếp theo thứ tự hiển thị tăng dần
        response.setEducations(
                cvEducationRepository
                        .findAllByCvVersionIdAndDeletedFalseOrderBySortOrderAsc(versionId)
                        .stream()
                        .map(cvMapper::toEducationResponse)
                        .toList()
        );

        // 11. Lấy danh sách Kinh nghiệm (Experiences) chưa xóa, xếp theo thứ tự hiển thị tăng dần
        response.setExperiences(
                cvExperienceRepository
                        .findAllByCvVersionIdAndDeletedFalseOrderBySortOrderAsc(versionId)
                        .stream()
                        .map(cvMapper::toExperienceResponse)
                        .toList()
        );

        // 12. Lấy danh sách Dự án (Projects) chưa xóa, xếp theo thứ tự hiển thị tăng dần
        response.setProjects(
                cvProjectRepository
                        .findAllByCvVersionIdAndDeletedFalseOrderBySortOrderAsc(versionId)
                        .stream()
                        .map(cvMapper::toProjectResponse)
                        .toList()
        );

        // 13. Lấy danh sách Chứng chỉ (Certificates) chưa xóa, xếp theo thứ tự hiển thị tăng dần
        response.setCertificates(
                cvCertificateRepository
                        .findAllByCvVersionIdAndDeletedFalseOrderBySortOrderAsc(versionId)
                        .stream()
                        .map(cvMapper::toCertificateResponse)
                        .toList()
        );

        // 14. Lấy danh sách Ngoại ngữ (Languages) chưa xóa, xếp theo thứ tự hiển thị tăng dần
        response.setLanguages(
                cvLanguageRepository
                        .findAllByCvVersionIdAndDeletedFalseOrderBySortOrderAsc(versionId)
                        .stream()
                        .map(cvMapper::toLanguageResponse)
                        .toList()
        );

        // Đóng gói và trả về toàn bộ dữ liệu chi tiết của CV
        return response;
    }

    /**
     * Hủy bản nháp CV.
     *
     * Chỉ cho phép EMPLOYEE hoặc TECH_LEAD
     * hủy bản DRAFT của chính mình.
     *
     * DRAFT -> ARCHIVED
     */
    @Override
    public void cancelDraft(Long versionId) {

        // =====================================================
        // 1. Tìm CV Version
        // =====================================================

        CVVersion version =
                cvVersionRepository
                        .findByIdAndDeletedFalse(versionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "CV version không tồn tại"
                                )
                        );

        // =====================================================
        // 2. Chỉ cho phép hủy version đang ở DRAFT
        // =====================================================

        if (version.getStatus() != CvVersionStatus.DRAFT){
            throw new IllegalStateException(
                    "Chỉ có thể hủy CV đang ở trạng thái DRAFT"
            );
        }

        // =====================================================
        // 3. Lấy EmployeeCV
        // =====================================================

        EmployeeCV employeeCV = version.getEmployeeCV();

        if (employeeCV == null
                || employeeCV.isDeleted()){

            throw new ResourceNotFoundException(
                    "CV không tồn tại"
            );
        }

        // =====================================================
        // 4. Lấy Employee sở hữu CV
        // =====================================================

        Employee employee = employeeCV.getEmployee();

        if (employee == null
                || employee.isDeleted()){

            throw new ResourceNotFoundException(
                    "Nhân viên không tồn tại"
            );
        }

    // =====================================================
    // 5. Kiểm tra quyền hủy DRAFT
    // =====================================================

        System.out.println("=== CANCEL CV DEBUG ===");
        System.out.println("versionId = " + versionId);
        System.out.println("employeeId = " + employee.getId());
        System.out.println("employeeName = " + employee.getFullName());

        boolean canCancel =
                employeeAuthorizationService
                        .canCancelCVDraft(employee);

        System.out.println("canCancelCVDraft = " + canCancel);

        if (!canCancel) {
            throw new AccessDeniedException(
                    "Bạn không có quyền hủy bản nháp CV này"
            );
        }

        // =====================================================
        // 6. DRAFT -> ARCHIVED
        // =====================================================
        version.setStatus(CvVersionStatus.ARCHIVED);

        // =====================================================
        // 7. Xóa rejection reason nếu có
        // =====================================================

        version.setRejectionReason(null);

        // =====================================================
        // 8. Lưu version
        // =====================================================

        cvVersionRepository.save(version);

    }

    /*
    * Hàm xóa CV
    * không cho xóa CV khi đang có version đang xử lý
    * */
    @Override
    public void delete(Long id) {

        EmployeeCV employeeCV =
                employeeCVRepository
                        .findActiveById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "CV không tồn tại"
                                )
                        );

        if (hasActiveWorkflowVersion(employeeCV.getId())){
            throw new IllegalStateException(
                    "CV đang có phiên bản đang được xử lý, không thể xóa"
            );
        }

        employeeCV.setDeleted(true);

        employeeCVRepository.save(employeeCV);
    }


    /**
     * Cập nhật CV hiện tại.
     *
     * @param id      ID của EmployeeCV
     * @param request Dữ liệu CV mới
     * @return CV sau khi cập nhật
     */
    @Override
    @Transactional
    public CVDetailResponse update(Long id, CVUpdateRequest request) {

        // =====================================================
        // 1. TÌM CV
        // =====================================================

        EmployeeCV employeeCV = employeeCVRepository
                .findActiveById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("CV không tồn tại"));

        Employee employee = employeeCV.getEmployee();

        // =====================================================
        // 2. KIỂM TRA QUYỀN CẬP NHẬT
        // =====================================================

        if (!employeeAuthorizationService.canUpdateCV(employee)) {
            throw new AccessDeniedException(
                    "Bạn không có quyền cập nhật CV này"
            );
        }

        if (hasActiveWorkflowVersion(employeeCV.getId())) {
            throw new IllegalStateException(
                    "CV đang có một phiên bản đang được xử lý"
            );
        }


        // =====================================================
        // 3. TẠO VERSION DRAFT MỚI
        // =====================================================

        String newVersionNumber =
                generateNextVersion(employeeCV.getId());

        CVVersion newVersion = new CVVersion();

        newVersion.setEmployeeCV(employeeCV);
        newVersion.setVersion(newVersionNumber);

        // QUAN TRỌNG:
        // Bản sửa chỉ là bản nháp
        newVersion.setStatus(CvVersionStatus.DRAFT);

        // K phải version hiện tại
        newVersion.setIsCurrent(false);

        // Chưa bị từ chối nên chưa có lý do
        newVersion.setRejectionReason(null);

        cvVersionRepository.save(newVersion);

        // =====================================================
        // 4. LƯU PROFILE CỦA VERSION DRAFT
        // =====================================================

        if (request.getProfile() != null) {

            CVProfile profile =
                    cvMapper.toEntity(request.getProfile());

            profile.setCvVersion(newVersion);

            cvProfileRepository.save(profile);
        }

        // =====================================================
        // 5. LƯU SKILLS
        // =====================================================

        List<CVSkill> skills =
                Optional.ofNullable(request.getSkills())
                        .orElse(List.of())
                        .stream()
                        .map(cvMapper::toEntity)
                        .peek(skill -> skill.setCvVersion(newVersion))
                        .toList();

        for (int i = 0; i < skills.size(); i++) {
            skills.get(i).setSortOrder(i);
        }

        if (!skills.isEmpty()) {
            cvSkillRepository.saveAll(skills);
        }

        // =====================================================
        // 6. LƯU EDUCATIONS
        // =====================================================

        List<CVEducation> educations =
                Optional.ofNullable(request.getEducations())
                .orElse(List.of())
                .stream()
                .map(cvMapper::toEntity)
                .peek(education -> education.setCvVersion(newVersion))
                .toList();

        if (!educations.isEmpty()) {
            cvEducationRepository.saveAll(educations);
        }

        // =====================================================
        // 7. LƯU EXPERIENCES
        // =====================================================

        List<CVExperience> experiences =
                Optional.ofNullable(request.getExperiences())
                .orElse(List.of())
                .stream()
                .map(cvMapper::toEntity)
                .peek(experience -> experience.setCvVersion(newVersion))
                .toList();

        if (!experiences.isEmpty()) {
            cvExperienceRepository.saveAll(experiences);
        }

        // =====================================================
        // 8. LƯU PROJECTS
        // =====================================================

        List<CVProject> projects =
                Optional.ofNullable(request.getProjects())
                .orElse(List.of())
                .stream()
                .map(cvMapper::toEntity)
                .peek(project -> project.setCvVersion(newVersion))
                .toList();

        if (!projects.isEmpty()) {
            cvProjectRepository.saveAll(projects);
        }

        // =====================================================
        // 9. LƯU CERTIFICATES
        // =====================================================

        List<CVCertificate> certificates =
                Optional.ofNullable(request.getCertificates())
                .orElse(List.of())
                .stream()
                .map(cvMapper::toEntity)
                .peek(certificate -> certificate.setCvVersion(newVersion))
                .toList();

        if (!certificates.isEmpty()) {
            cvCertificateRepository.saveAll(certificates);
        }

        // =====================================================
        // 10. LƯU LANGUAGES
        // =====================================================

        List<CVLanguage> languages =
                Optional.ofNullable(request.getLanguages())
                .orElse(List.of())
                .stream()
                .map(cvMapper::toEntity)
                .peek(language -> language.setCvVersion(newVersion))
                .toList();

        if (!languages.isEmpty()) {
            cvLanguageRepository.saveAll(languages);
        }

        // =====================================================
        // 11. KHÔNG THAY ĐỔI CURRENT VERSION
        // =====================================================

        /*
         * CỰC KỲ QUAN TRỌNG:
         *
         * Không được:
         *
         * employeeCV.setCurrentVersion(newVersion);
         *
         * Không được:
         *
         * currentVersion.setIsCurrent(false);
         * currentVersion.setStatus(ARCHIVED);
         *
         * Vì V1.0 vẫn là CV chính thức.
         */

        // EmployeeCV vẫn giữ nguyên currentVersion.
        // Không cần save EmployeeCV vì không có thay đổi.

        // =====================================================
        // 12. TRẢ VỀ VERSION DRAFT
        // =====================================================

        return getDetail(employeeCV.getId());
    }

    // Hàm gửi bản nháp CV
    @Override
    public CVDetailResponse submitDraft(Long versionId) {

        // =====================================================
        // 1. Tìm CV Version
        // =====================================================
        CVVersion version = cvVersionRepository
                .findByIdAndDeletedFalse(versionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CV version không tồn tại"
                ));

        // =====================================================
        // 2. Chỉ DRAFT mới được gửi duyệt
        // =====================================================
        if (version.getStatus() != CvVersionStatus.DRAFT) {
            throw new IllegalStateException(
                    "Chỉ bản nháp mới được gửi duyệt"
            );
        }

        // =====================================================
        // 3. Lấy EmployeeCV
        // =====================================================
        EmployeeCV employeeCV = version.getEmployeeCV();

        if (employeeCV == null || employeeCV.isDeleted()) {
            throw new ResourceNotFoundException(
                    "CV không tồn tại"
            );
        }

        // =====================================================
        // 4. Lấy Employee sở hữu CV
        // =====================================================
        Employee employee = employeeCV.getEmployee();

        if (employee == null || employee.isDeleted()) {

            throw new ResourceNotFoundException(
                    "Nhân viên sở hữu CV không tồn tại"
            );
        }

        // =====================================================
        // 5. Kiểm tra quyền submit
        // =====================================================
        if (!employeeAuthorizationService.canSubmitCV(employee)){
            throw new AccessDeniedException("Bạn không có quyền gửi bản CV này");
        }

        // =====================================================
        // 6. Lấy user hiện tại
        // =====================================================
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new AccessDeniedException("Người dùng chưa được xác thực");
        }

        String username = authentication.getName();

        User currentUser =
                userService.findByUsername(username);

        // =====================================================
        // 7. Kiểm tra role
        // =====================================================
        boolean isEmployee =
                currentUser.getUserRoles()
                        .stream()
                        .anyMatch(userRole ->
                                userRole.getRole()
                                        .getName()
                                        .equals(RoleName.EMPLOYEE.name())
                        );

        boolean isTechLead =
                currentUser.getUserRoles()
                        .stream()
                        .anyMatch(userRole ->
                                userRole.getRole()
                                        .getName()
                                        .equals(RoleName.TECH_LEAD.name())
                                );

        // =====================================================
        // 8. Xác định trạng thái tiếp theo
        // =====================================================

        if (isEmployee){

            /*
             * EMPLOYEE:
             *
             * DRAFT
             *   ↓
             * PENDING_TECH_LEAD
             */
            version.setStatus(
                    CvVersionStatus.PENDING_TECH_LEAD
            );
        } else if (isTechLead) {

            /*
             * TECH_LEAD:
             *
             * DRAFT
             *   ↓
             * PENDING_HR
             */
            version.setStatus(
                    CvVersionStatus.PENDING_HR
            );

        } else {

            throw new AccessDeniedException(
                    "Role hiện tại không được phép gửi bản nháp CV"
            );
        }

        // =====================================================
        // 9. Nếu submit lại sau khi bị từ chối,
        //    xóa lý do từ chối cũ
        // =====================================================
        version.setRejectionReason(null);

        // =====================================================
        // 10. Lưu trạng thái mới
        // =====================================================
        cvVersionRepository.save(version);

        // =====================================================
        // 11. Trả về CV sau khi submit
        // =====================================================
        return getDetail(employeeCV.getId());
    }

    // Hàm bổ trợ cho việc thông báo đến TECH LEAD về việc gửi bản CV
    private void notifyTechLeadsAboutSubmittedCV(
            CVVersion version,
            Employee employee
    ) {

        Long departmentId =
                employee.getDepartment().getId();

        List<User> techLeads =
                userRepository.findUsersByDepartmentAndRole(
                        departmentId,
                        RoleName.TECH_LEAD.name()
                );

        for (User techLead : techLeads) {

            notificationService.createNotification(
                    techLead.getId(),
                    NotificationType.CV_SUBMITTED,
                    "CV được gửi duyệt",
                    "CV của nhân viên"
                                + employee.getFullName()
                                + " phiên bản"
                                + version.getVersion()
                                + " đã được gửi và đang chờ Tech Lead duyệt.",
                    "CV_VERSION",
                    version.getId()
            );
        }
    }

    // Hàm kiểm tra quyền chấp nhận CV bởi Techlead
    @Override
    @Transactional
    public CVDetailResponse approveByTechLead(Long versionId) {

        // =====================================================
        // 1. Tìm CV Version
        // =====================================================
        CVVersion version = cvVersionRepository
                .findByIdAndDeletedFalse(versionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "CV version không tồn tại"
                        )
                );

        // =====================================================
        // 2. Chỉ PENDING_TECH_LEAD mới được Tech Lead duyệt
        // =====================================================
        if (version.getStatus() != CvVersionStatus.PENDING_TECH_LEAD) {
            throw new IllegalStateException(
                    "CV không ở trạng thái chờ Tech Lead duyệt"
            );
        }

        // =====================================================
        // 3. Lấy EmployeeCV
        // =====================================================
        EmployeeCV employeeCV = version.getEmployeeCV();

        if (employeeCV == null || employeeCV.isDeleted()) {

            throw new ResourceNotFoundException(
                    "CV không tồn tại"
            );
        }

        // =====================================================
        // 4. Lấy Employee sở hữu CV
        // =====================================================
        Employee employee = employeeCV.getEmployee();

        if (employee == null || employee.isDeleted()) {
            throw new ResourceNotFoundException("Nhân viên sở hữu CV không tồn tại");
        }

        // =====================================================
        // 5. Kiểm tra quyền Tech Lead
        // =====================================================
        if (!employeeAuthorizationService.canApproveCVByTechLead(employee)) {
            throw new AccessDeniedException("Bạn không có quyền duyệt CV này");
        }

        // =====================================================
        // 6. Tech Lead duyệt
        //
        // PENDING_TECH_LEAD
        //        ↓
        // PENDING_HR
        // =====================================================
        version.setStatus(
                CvVersionStatus.PENDING_HR
        );

        // Khi approve thì không còn lý do từ chối
        version.setRejectionReason(null);

        // =====================================================
        // 7. Lưu
        // =====================================================
        cvVersionRepository.save(version);

        // =====================================================
        // 8. Trả về CV
        // =====================================================
        return getDetail(employeeCV.getId());
    }

    // Phương thức HR chấp nhận version cv
    @Override
    public CVDetailResponse approveByHr(Long versionId) {

        // =====================================================
        // 1. Tìm CV Version
        // =====================================================

        CVVersion version = cvVersionRepository
                .findByIdAndDeletedFalse(versionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "CV version không tồn tại"
                        )
                );

        // =====================================================
        // 2. Chỉ PENDING_HR mới được HR/Admin duyệt
        // =====================================================

        if (version.getStatus() != CvVersionStatus.PENDING_HR) {
            throw new IllegalStateException(
                    "CV không ở trạng thái chờ HR duyệt"
            );
        }

        // =====================================================
        // 3. Lấy EmployeeCV
        // =====================================================

        EmployeeCV employeeCV = version.getEmployeeCV();

        if (employeeCV == null || employeeCV.isDeleted()) {

            throw new ResourceNotFoundException(
                    "CV không tồn tại"
            );
        }

        // =====================================================
        // 4. Lấy Employee sở hữu CV
        // =====================================================

        Employee employee = employeeCV.getEmployee();

        if (employee == null || employee.isDeleted()) {

            throw new ResourceNotFoundException(
                    "Nhân viên sở hữu CV không tồn tại"
            );
        }

        // =====================================================
        // 5. Kiểm tra quyền HR/Admin
        // =====================================================

        if (!employeeAuthorizationService.canApproveCVByHr()) {
            throw new AccessDeniedException(
                    "Bạn không có quyền duyệt CV này"
            );
        }

        // =====================================================
        // 6. Chuyển thành OFFICIAL
        // =====================================================

        version.setStatus(CvVersionStatus.OFFICIAL);

        version.setIsCurrent(true);

        version.setRejectionReason(null);

        // =====================================================
        // 7. Tắt current của version cũ
        // =====================================================

        CVVersion currentVersion =
                employeeCV.getCurrentVersion();

        if (currentVersion != null
                && !currentVersion.getId().equals(version.getId())) {

            currentVersion.setIsCurrent(false);

            cvVersionRepository.save(currentVersion);
        }

        // =====================================================
        // 8. Đặt version mới thành current
        // =====================================================

        employeeCV.setCurrentVersion(version);

        employeeCV.setStatus(CvStatus.UPDATED);

        // =====================================================
        // 9. Lưu
        // =====================================================

        cvVersionRepository.save(version);

        employeeCVRepository.save(employeeCV);

        // =====================================================
        // 10. Trả về CV
        // =====================================================

        return getDetail(employeeCV.getId());
    }

    // Hàm xử lý từ chối từ Techlead
    @Override
    public CVDetailResponse rejectByTechLead(Long versionId, String rejectionReason) {

        // =====================================================
        // 1. Kiểm tra lý do từ chối
        // =====================================================
        if (rejectionReason == null
                || rejectionReason.isBlank()) {

            throw new IllegalArgumentException(
                    "Lý do từ chối không được để trống"
            );
        }

        // =====================================================
        // 2. Tìm CV Version
        // =====================================================
        CVVersion version = cvVersionRepository
                .findByIdAndDeletedFalse(versionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "CV version không tồn tại"
                        )
                );

        // =====================================================
        // 3. Chỉ PENDING_TECH_LEAD mới được từ chối
        // =====================================================
        if (version.getStatus()
                != CvVersionStatus.PENDING_TECH_LEAD) {

            throw new IllegalStateException(
                    "CV không ở trạng thái chờ Tech Lead duyệt"
            );
        }

        // =====================================================
        // 4. Lấy EmployeeCV
        // =====================================================
        EmployeeCV employeeCV = version.getEmployeeCV();

        if (employeeCV == null
                || employeeCV.isDeleted()) {

            throw new ResourceNotFoundException(
                    "CV không tồn tại"
            );
        }

        // =====================================================
        // 5. Lấy Employee sở hữu CV
        // =====================================================
        Employee employee = employeeCV.getEmployee();

        if (employee == null
                || employee.isDeleted()) {

            throw new ResourceNotFoundException(
                    "Nhân viên sở hữu CV không tồn tại"
            );
        }

        // =====================================================
        // 6. Kiểm tra quyền Tech Lead
        // =====================================================
        if (!employeeAuthorizationService.canApproveCVByTechLead(employee)){
            throw new AccessDeniedException("Bạn không có quyền từ chối CV này");
        }

        // =====================================================
        // 7. Chuyển trạng thái sang TECH_LEAD_REJECTED
        // =====================================================
        version.setStatus(
                CvVersionStatus.TECH_LEAD_REJECTED
        );

        // =====================================================
        // 8. Lưu lý do từ chối
        // =====================================================
        version.setRejectionReason(
                rejectionReason.trim()
        );

        // =====================================================
        // 9. Lưu
        // =====================================================
        cvVersionRepository.save(version);

        // =====================================================
        // 10. Trả về CV
        // =====================================================
        return getDetail(employeeCV.getId());
    }

    // Hàm xử lý nghiệp vụ khi ADMIN/HR từ chối chấp nhận với version cv
    @Override
    public CVDetailResponse rejectByHr(
            Long versionId,
            String rejectionReason
    ) {

        // =====================================================
        // 1. Kiểm tra lý do từ chối
        // =====================================================
        if (rejectionReason == null
                || rejectionReason.isBlank()){

            throw new IllegalArgumentException(
                    "Lý do từ chối không được để trống"
            );
        }

        // =====================================================
        // 2. Tìm CV Version
        // =====================================================
        CVVersion version = cvVersionRepository
                .findByIdAndDeletedFalse(versionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "CV version không tồn tại"
                        )
                );

        // =====================================================
        // 3. Chỉ PENDING_HR/ADMIN mới được từ chối
        // =====================================================
        if (version.getStatus()
                != CvVersionStatus.PENDING_HR) {

            throw new IllegalStateException(
                    "CV không ở trạng thái chờ HR duyệt"
            );
        }

        // =====================================================
        // 4. Lấy EmployeeCV
        // =====================================================
        EmployeeCV employeeCV = version.getEmployeeCV();

        if (employeeCV == null
                || employeeCV.isDeleted()){

            throw new ResourceNotFoundException(
                    "CV không tồn tại"
            );
        }

        // =====================================================
        // 5. Lấy Employee sở hữu CV
        // =====================================================
        Employee employee = employeeCV.getEmployee();

        if (employee == null
                || employee.isDeleted()) {

            throw new ResourceNotFoundException(
                    "Nhân viên sở hữu CV không tồn tại"
            );
        }

        // =====================================================
        // 6. Kiểm tra quyền HR/Admin
        // =====================================================
        if (!employeeAuthorizationService.canApproveCVByHr()) {

            throw new AccessDeniedException(
                    "Bạn không có quyền từ chối CV này"
            );
        }

        // =====================================================
        // 7. Chuyển trạng thái sang HR_REJECTED
        // =====================================================
        version.setStatus(
                CvVersionStatus.HR_REJECTED
        );

        // =====================================================
        // 8. Lưu lý do từ chối
        // =====================================================
        version.setRejectionReason(
                rejectionReason.trim()
        );

        // =====================================================
        // 9. Lưu
        // =====================================================
        cvVersionRepository.save(version);

        // =====================================================
        // 10. Trả về CV
        // =====================================================
        return getDetail(employeeCV.getId());
    }


    /**
     * Hàm hỗ trợ tự động tạo đối tượng Phân trang và Sắp xếp (Pageable) từ Request của Client gửi lên.
     *
     * @param request Đối tượng chứa các tham số tìm kiếm, phân trang từ giao diện (Page, Size, SortBy...)
     * @return Pageable Đối tượng chuẩn của Spring để truyền vào tầng Repository truy vấn DB
     */
    private Pageable createPageable(CVSearchRequest request) {

        // 1. Phân tích cột cần sắp xếp: Chuyển đổi tên trường từ giao diện gửi lên (Ví dụ: "name")
        // sang đúng tên thuộc tính trong file Entity Java (ví dụ: "fullName").
        String sortProperty = resolveSortProperty(request.getSortBy());

        // 2. Phân tích hướng sắp xếp: chuyển đổi chuỗi chữ "asc" hoặc "desc" từ request
        // thành đối tượng Enum Sort.Direction (ASC: tăng dần, DESC: giảm dần) của Spring.
        Sort.Direction direction =
                Sort.Direction.fromString(request.getSortDirection());

        // 3. Khởi tạo và trả về đối tượng PageRequest (triển khai từ interface Pageable)
        // chứa 3 tham số cốt lõi: Số trang muốn xem, Số dòng trên mỗi trang, và Quy tắc sắp xếp.
        return PageRequest.of(
                request.getPage(),                 // Số thứ tự trang cần lấy (Bắt đầu từ số 0)
                request.getSize(),                 // Số lượng dòng dữ liệu hiển thị trên 1 trang (ví dụ: 10, 20 dòng) đã set cứng trong DTO Request cụ thể ở CVSearchRequest
                Sort.by(direction, sortProperty)   // Quy tắc sắp xếp: Hướng sắp xếp + Cột sắp xếp
        );
    }

    /**
     * Hàm hỗ trợ kiểm tra và chuyển đổi tên cột cần sắp xếp từ giao diện gửi lên
     * thành đúng tên thuộc tính (Field) nằm trong câu lệnh truy vấn JPQL/Entity.
     *
     * @param sortBy Tên cột cần sắp xếp do Client gửi lên (ví dụ: "employeeName")
     * @return String Tên thuộc tính chuẩn xác để đưa vào bộ lọc của Spring Data JPA
     */
    private String resolveSortProperty(String sortBy) {

        // 1. Kiểm tra phòng hờ: Nếu người dùng không truyền cột cần sắp xếp (null hoặc để trống "")
        // thì hệ thống sẽ tự động lấy cột mặc định là "id" để sắp xếp
        if (sortBy == null || sortBy.isBlank()) {
            return "id";
        }

        // 2. Sử dụng cấu trúc Switch-Expression (Java 14+) để đối chiếu từ khóa:
        // Cứ khớp với chữ nào ở vế trái (Client gửi), sẽ trả về đúng chữ ở vế phải (Database nhận).
        return switch (sortBy) {
            case "id" -> "id";                           // Sắp xếp theo ID của CV
            case "status" -> "status";                   // Sắp xếp theo Trạng thái của CV

            // 3. Chốt chặn an toàn (Whitelisting): Nếu người dùng cố tình truyền lên một từ khóa lạ hoắc
            // không nằm trong danh sách trên (ví dụ: "password", "salary"), hệ thống sẽ bỏ qua
            // và tự động quay về sắp xếp theo cột "id", giúp ứng dụng KHÔNG BỊ SẬP (Lỗi SQL).
            default -> "id";
        };
    }

    /**
     * Hàm hỗ trợ kiểm tra và làm sạch từ khóa tìm kiếm (Keyword) từ giao diện gửi lên.
     * Đảm bảo từ khóa đạt tiêu chuẩn trước khi đưa vào câu lệnh truy vấn SQL/JPQL.
     *
     * @param keyword Từ khóa thô do người dùng nhập vào ô tìm kiếm (ví dụ: "  NV01   ")
     * @return String Từ khóa đã được làm sạch, hoặc null nếu người dùng không nhập gì
     */
    private String normalizeKeyword(String keyword) {

        // 1. Kiểm tra phòng hờ: Nếu người dùng không nhập từ khóa (null), hoặc chỉ gõ toàn dấu cách
        // khoảng trắng mà không có chữ ("   "), hàm sẽ lập tức trả về giá trị null.
        // Việc trả về null giúp câu lệnh SQL hiểu rằng "Bộ lọc từ khóa này được BỎ QUA".
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        // 2. Nếu từ khóa hợp lệ: Hàm sử dụng lệnh .trim() để tự động "gọt bỏ" tất cả các khoảng trắng
        // dư thừa ở hai đầu (đầu và cuối) của chuỗi văn bản.
        return keyword.trim();
    }

    /* Hàm tự động sinh số phiên bản cv tiếp theo dựa vào phiên bản hiện tại (ví dụ: V1.0 -> V1.1)*/
    private String generateNextVersion(Long employeeCvId) {

        // 1. Lấy tất cả version đã tồn tại của CV
        List<String> versions =
                cvVersionRepository.findAllVersionNumbers(employeeCvId);

        // 2. Nếu CV chưa có version nào
        if (versions == null || versions.isEmpty()) {
            return INITIAL_VERSION;
        }

        int maxMajor = 0;
        int maxMinor = -1;

        // 3. Duyệt toàn bộ version hiện có
        for (String version : versions) {

            // Bỏ qua dữ liệu null/rỗng
            if (version == null || version.isBlank()) {
                continue;
            }

            // 4. Version phải bắt đầu bằng V
            if (!version.startsWith("V")) {
                throw new IllegalArgumentException(
                        "Version CV không hợp lệ: " + version
                );
            }

            // 5. Bỏ chữ V
            String numberPart = version.substring(1);

            // 6. Tách major và minor
            String[] parts = numberPart.split("\\.");

            // 7. Phải có đúng 2 phần
            if (parts.length != 2) {
                throw new IllegalArgumentException(
                        "Version CV không hợp lệ: " + version
                );
            }

            try {
                // 8. Chuyển major/minor sang số
                int major = Integer.parseInt(parts[0]);
                int minor = Integer.parseInt(parts[1]);

                // 9. Tìm version lớn nhất
                if (major > maxMajor
                        || (major == maxMajor && minor > maxMinor)) {

                    maxMajor = major;
                    maxMinor = minor;
                }

            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Version CV không hợp lệ: " + version,
                        e
                );
            }
        }

        // 10. Nếu không tìm được version hợp lệ
        if (maxMinor < 0) {
            return INITIAL_VERSION;
        }

        // 11. Tăng minor lên 1
        return "V" + maxMajor + "." + (maxMinor + 1);
    }

    // Phương thức lấy toàn bộ các version
    private boolean hasActiveWorkflowVersion(Long employeeCvId) {

        List<CVVersion> versions =
                cvVersionRepository
                        .findAllByEmployeeCVIdAndDeletedFalseOrderByCreatedDateDesc(
                                employeeCvId
                        );

        return versions.stream()
                .anyMatch(version ->
                        version.getStatus() == CvVersionStatus.DRAFT
                                || version.getStatus() == CvVersionStatus.PENDING_TECH_LEAD
                                || version.getStatus() == CvVersionStatus.PENDING_HR
                );
    }
}
