package systemanagercv.example.systemanagercv.cv.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import java.util.List;


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

    private final EmployeeRepository employeeRepository;
    private final CVMapper cvMapper;
    private final EmployeeAuthorizationService employeeAuthorizationService;

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
     * Cập nhật CV hiện tại.
     *
     * @param id      ID của EmployeeCV
     * @param request Dữ liệu CV mới
     * @return CV sau khi cập nhật
     */
    @Override
    public CVDetailResponse update(Long id, CVUpdateRequest request) {

        // 1. Tìm CV đang hoạt động
        EmployeeCV employeeCV = employeeCVRepository
                .findActiveById(id)
                .orElseThrow(() ->
                        new RuntimeException("CV không tồn tại"));

        // 2. Lấy nhân viên sở hữu CV
        Employee employee = employeeCV.getEmployee();

        // 3. Kiểm rta quyền cập nhật CV
        if (!employeeAuthorizationService.canUpdateCV(employee)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Bạn không có quyền cập nhật CV này"
            );
        }

        // 4. Lấy version hiện tại
        CVVersion currentVersion = cvVersionRepository
                .findByEmployeeCVIdAndIsCurrentTrueAndDeletedFalse(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy version hiện tại của CV")
                );

        // 5. Tạo version mới
        String newVersionNumber =
                generateNextVersion(currentVersion.getVersion());

        CVVersion newVersion = new CVVersion();

        newVersion.setEmployeeCV(employeeCV);
        newVersion.setVersion(newVersionNumber);
        newVersion.setStatus(CvVersionStatus.OFFICIAL);
        newVersion.setIsCurrent(true);

        // 6. Version cũ không còn là current
        currentVersion.setIsCurrent(false);

        cvVersionRepository.save(currentVersion);
        cvVersionRepository.save(newVersion);

        // 7. Cập nhật Profile
        if (request.getProfile() != null) {

            CVProfile profile =
                    cvMapper.toEntity(request.getProfile());

            profile.setCvVersion(newVersion);

            cvProfileRepository.save(profile);
        }

        // 8. Cập nhật Skills
        List<CVSkill> skills = request.getSkills()
                .stream()
                .map(cvMapper::toEntity)
                .peek(skill -> skill.setCvVersion(newVersion))
                .toList();

        if (!skills.isEmpty()) {
            cvSkillRepository.saveAll(skills);
        }

        // 9. Cập nhật Education
        List<CVEducation> educations = request.getEducations()
                .stream()
                .map(cvMapper::toEntity)
                .peek(education -> education.setCvVersion(newVersion))
                .toList();

        if (!educations.isEmpty()) {
            cvEducationRepository.saveAll(educations);
        }

        // 10. Cập nhật Experience
        List<CVExperience> experiences = request.getExperiences()
                .stream()
                .map(cvMapper::toEntity)
                .peek(experience -> experience.setCvVersion(newVersion))
                .toList();

        if (!experiences.isEmpty()) {
            cvExperienceRepository.saveAll(experiences);
        }

        // 11. Cập nhật Project
        List<CVProject> projects = request.getProjects()
                .stream()
                .map(cvMapper::toEntity)
                .peek(project -> project.setCvVersion(newVersion))
                .toList();

        if (!projects.isEmpty()) {
            cvProjectRepository.saveAll(projects);
        }

        // 12. Cập nhật Certificate
        List<CVCertificate> certificates = request.getCertificates()
                .stream()
                .map(cvMapper::toEntity)
                .peek(certificate -> certificate.setCvVersion(newVersion))
                .toList();

        if (!certificates.isEmpty()) {
            cvCertificateRepository.saveAll(certificates);
        }

        // 13. Cập nhật Language
        List<CVLanguage> languages = request.getLanguages()
                .stream()
                .map(cvMapper::toEntity)
                .peek(language -> language.setCvVersion(newVersion))
                .toList();

        if (!languages.isEmpty()) {
            cvLanguageRepository.saveAll(languages);
        }

        // 14. Cập nhật currentVersion của EmployeeCV
        employeeCV.setCurrentVersion(newVersion);

        // 15. CV sau khi update có trạng thái Đã cập nhật
        employeeCV.setStatus(CvStatus.UPDATED);

        employeeCVRepository.save(employeeCV);

        // 16. Trả về CV mới nhất
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
    private String generateNextVersion(String currentVersion){

        // 1. Nếu chưa có phiên bản nào (null hoặc chuỗi rỗng), mặc định khởi tạo là "v1.0"
        if (currentVersion == null || currentVersion.isBlank()){
            return "V1.0";
        }

        // 2. Định dạng bắt buộc phải bắt đầu bằng chữ "v", nếu k thì báo lỗi dữ liệu không phù hợp
        if (!currentVersion.startsWith("V")){
            throw new IllegalArgumentException(
                    "Version CV không hợp lệ: " + currentVersion
            );
        }

        // 3. Cắt bỏ chữ "v" ở đầu để lấy phần chuỗi số phía sau (Ví dụ: "v1.2" -> "1.2")
        String numberPart =
                currentVersion.substring(1);

        // 4. Tách chuỗi số bằng dấu chấm "." thành 2 phần:[phiên bản chính, phiên bản phụ]
        String[] parts =
                numberPart.split("\\.");

        // 5. Định dạng bắt buộc phải có đúng 2 phần tách biệt bởi dấu chấm (Ví dụ: "1" và "2"), nếu không thì báo lỗi
        if (parts.length != 2){
            throw new IllegalArgumentException(
                    "Version CV không hợp lệ: " + currentVersion
            );
        }

        // 6. Chuyển đổi 2 phần từ chuỗi ký tự sang số nguyên để tính toán
        int major = Integer.parseInt(parts[0]); // Số đứng trước dấu chấm (Major)
        int minor = Integer.parseInt(parts[1]); // Số đứng sau dấu chấm (Minor)

        // 7. Tăng số phiên bản phụ lên 1 đơn vị (Ví dụ: từ .2 lên .3)
        minor++;


        // 8. Ghép lại thành chuỗi định dạng hoàn chỉnh và trả về (Ví dụ: "V1.3")
        return "V" + major + "." + minor;
    }
}
