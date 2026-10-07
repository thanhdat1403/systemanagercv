package systemanagercv.example.systemanagercv.cv.update_request.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import systemanagercv.example.systemanagercv.common.enums.RoleName;
import systemanagercv.example.systemanagercv.common.exception.BusinessException;
import systemanagercv.example.systemanagercv.common.exception.ResourceNotFoundException;

import systemanagercv.example.systemanagercv.cv.entity.EmployeeCV;
import systemanagercv.example.systemanagercv.cv.enums.CvStatus;
import systemanagercv.example.systemanagercv.cv.repository.EmployeeCVRepository;

import systemanagercv.example.systemanagercv.cv.update_request.dto.request.CVUpdateRequestCreateRequest;
import systemanagercv.example.systemanagercv.cv.update_request.dto.request.CVUpdateRequestSearchRequest;
import systemanagercv.example.systemanagercv.cv.update_request.dto.response.CVUpdateRequestResponse;
import systemanagercv.example.systemanagercv.cv.update_request.entity.CVUpdateRequestEntity;
import systemanagercv.example.systemanagercv.cv.update_request.enums.CVUpdateRequestStatus;
import systemanagercv.example.systemanagercv.cv.update_request.mapper.CVUpdateRequestMapper;
import systemanagercv.example.systemanagercv.cv.update_request.repository.CVUpdateRequestRepository;
import systemanagercv.example.systemanagercv.cv.update_request.service.CVUpdateRequestService;

import systemanagercv.example.systemanagercv.employee.authorization.EmployeeAuthorizationService;
import systemanagercv.example.systemanagercv.employee.entity.Employee;

import systemanagercv.example.systemanagercv.notification.enums.NotificationType;
import systemanagercv.example.systemanagercv.notification.service.NotificationService;

import systemanagercv.example.systemanagercv.user.entity.User;
import systemanagercv.example.systemanagercv.user.service.UserService;

import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class CVUpdateRequestServiceImpl
        implements CVUpdateRequestService {

    /**
     * Các trạng thái được xem là request đang hoạt động.
     *
     * COMPLETED và CANCELLED không được xem là active.
     */
    private static final List<CVUpdateRequestStatus>
            ACTIVE_REQUEST_STATUSES = List.of(
            CVUpdateRequestStatus.PENDING,
            CVUpdateRequestStatus.IN_PROGRESS
    );


    /**
     * Format deadline dùng trong notification.
     */
    private static final DateTimeFormatter
            DEADLINE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");


    private final CVUpdateRequestRepository
            cvUpdateRequestRepository;

    private final EmployeeCVRepository
            employeeCVRepository;

    private final CVUpdateRequestMapper
            cvUpdateRequestMapper;

    private final EmployeeAuthorizationService
            employeeAuthorizationService;

    private final NotificationService
            notificationService;

    private final UserService
            userService;


    // =====================================================
    // CREATE BATCH
    // =====================================================

    /**
     * HR tạo Update Request cho nhiều Employee trong một lần.
     *
     * Workflow:
     *
     * HR
     *   ↓
     * chọn nhiều Employee
     *   ↓
     * nhập content + deadline
     *   ↓
     * kiểm tra từng EmployeeCV
     *   ↓
     * kiểm tra active request
     *   ↓
     * tạo request PENDING
     *   ↓
     * EmployeeCV = NOT_UPDATED
     *   ↓
     * tạo notification cho từng Employee
     */
    @Override
    public List<CVUpdateRequestResponse> createBatch(
            CVUpdateRequestCreateRequest request
    ) {

        // =====================================================
        // 1. LẤY USER HIỆN TẠI
        // =====================================================

        User currentUser =
                getCurrentUser();


        // =====================================================
        // 2. CHỈ HR ĐƯỢC TẠO REQUEST
        // =====================================================

        requireHr(
                currentUser
        );


        // =====================================================
        // 3. VALIDATE REQUEST
        // =====================================================

        validateBatchRequest(
                request
        );


        // =====================================================
        // 4. CHECK DUPLICATE EMPLOYEE ID
        // =====================================================

        Set<Long> uniqueEmployeeIds =
                new HashSet<>(
                        request.getEmployeeIds()
                );


        if (
                uniqueEmployeeIds.size()
                        != request.getEmployeeIds().size()
        ) {

            throw new BusinessException(
                    "error.cv.updateRequest.duplicateEmployee"
            );

        }


        // =====================================================
        // 5. TẠO REQUEST CHO TỪNG EMPLOYEE
        // =====================================================

        return request.getEmployeeIds()
                .stream()
                .map(
                        employeeId ->
                                createSingleRequest(
                                        employeeId,
                                        request,
                                        currentUser
                                )
                )
                .toList();
    }


    // =====================================================
    // CREATE SINGLE REQUEST - INTERNAL
    // =====================================================

    /**
     * Tạo một Update Request cho một Employee.
     *
     * Method này chỉ được gọi nội bộ bởi createBatch().
     */
    private CVUpdateRequestResponse
    createSingleRequest(
            Long employeeId,
            CVUpdateRequestCreateRequest request,
            User currentUser
    ) {

        // =====================================================
        // 1. TÌM EMPLOYEE CV
        // =====================================================

        EmployeeCV employeeCV =
                employeeCVRepository
                        .findByEmployeeIdAndDeletedFalse(
                                employeeId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "CV của nhân viên ID "
                                                + employeeId
                                                + " không tồn tại"
                                )
                        );


        // =====================================================
        // 2. TÌM EMPLOYEE
        // =====================================================

        Employee employee =
                employeeCV.getEmployee();


        if (
                employee == null ||
                        employee.isDeleted()
        ) {

            throw new ResourceNotFoundException(
                    "Nhân viên ID "
                            + employeeId
                            + " không tồn tại"
            );

        }


        // =====================================================
        // 3. KIỂM TRA ACTIVE REQUEST
        // =====================================================

        boolean hasActiveRequest =
                cvUpdateRequestRepository
                        .existsActiveRequest(
                                employeeCV.getId(),
                                ACTIVE_REQUEST_STATUSES
                        );


        if (hasActiveRequest) {

            throw new BusinessException(
                    "error.cv.updateRequest.activeExists"
            );

        }


        // =====================================================
        // 4. TẠO ENTITY
        // =====================================================

        CVUpdateRequestEntity updateRequest =
                new CVUpdateRequestEntity();


        updateRequest.setEmployeeCV(
                employeeCV
        );


        updateRequest.setRequestedBy(
                currentUser
        );


        updateRequest.setStatus(
                CVUpdateRequestStatus.PENDING
        );


        updateRequest.setContent(
                request.getContent()
        );


        updateRequest.setDeadline(
                request.getDeadline()
        );


        // =====================================================
        // 5. SAVE REQUEST
        // =====================================================

        CVUpdateRequestEntity savedRequest =
                cvUpdateRequestRepository.save(
                        updateRequest
                );


        // =====================================================
        // 6. CV -> NOT_UPDATED
        // =====================================================

        employeeCV.setStatus(
                CvStatus.NOT_UPDATED
        );


        employeeCVRepository.save(
                employeeCV
        );


        // =====================================================
        // 7. LẤY USER CỦA EMPLOYEE
        // =====================================================

        User employeeUser =
                employee.getUser();


        if (employeeUser == null) {

            throw new BusinessException(
                    "error.cv.updateRequest.employeeUserNotFound"
            );

        }


        // =====================================================
        // 8. TẠO NOTIFICATION
        // =====================================================

        String notificationMessage =
                buildNotificationMessage(
                        request
                );


        notificationService.createNotification(
                employeeUser.getId(),

                NotificationType.CV_UPDATE_REQUESTED,

                "Có yêu cầu cập nhật CV",

                notificationMessage,

                "CV_UPDATE_REQUEST",

                savedRequest.getId()
        );


        // =====================================================
        // 9. RESPONSE
        // =====================================================

        return cvUpdateRequestMapper.toResponse(
                savedRequest
        );
    }


    // =====================================================
    // BUILD NOTIFICATION MESSAGE
    // =====================================================

    private String buildNotificationMessage(
            CVUpdateRequestCreateRequest request
    ) {

        return "HR yêu cầu bạn cập nhật CV. "
                + "Nội dung: "
                + request.getContent()
                + ". Deadline: "
                + request.getDeadline()
                .format(
                        DEADLINE_FORMATTER
                )
                + ".";
    }


    // =====================================================
    // VALIDATE BATCH REQUEST
    // =====================================================

    private void validateBatchRequest(
            CVUpdateRequestCreateRequest request
    ) {

        if (request == null) {

            throw new BusinessException(
                    "error.cv.updateRequest.invalidRequest"
            );
        }


        if (
                request.getEmployeeIds() == null ||
                        request.getEmployeeIds().isEmpty()
        ) {

            throw new BusinessException(
                    "error.cv.updateRequest.employeeIdsRequired"
            );

        }


        if (
                request.getContent() == null ||
                        request.getContent().isBlank()
        ) {

            throw new BusinessException(
                    "error.cv.updateRequest.contentRequired"
            );

        }


        if (
                request.getDeadline() == null
        ) {

            throw new BusinessException(
                    "error.cv.updateRequest.deadlineRequired"
            );

        }

    }


    // =====================================================
    // SEARCH
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public Page<CVUpdateRequestResponse> search(
            CVUpdateRequestSearchRequest request
    ) {

        User currentUser =
                getCurrentUser();


        Long employeeId;


        // =====================================================
        // HR
        // =====================================================

        if (
                hasRole(
                        currentUser,
                        RoleName.HR
                )
        ) {

            employeeId =
                    request.getEmployeeId();

        }


        // =====================================================
        // EMPLOYEE
        // =====================================================

        else if (
                hasRole(
                        currentUser,
                        RoleName.EMPLOYEE
                )
        ) {

            Employee currentEmployee =
                    currentUser.getEmployee();


            if (
                    currentEmployee == null ||
                            currentEmployee.isDeleted()
            ) {

                throw new AccessDeniedException(
                        "Employee chưa được gán hồ sơ nhân viên"
                );

            }


            /*
             * Employee luôn bị giới hạn
             * về chính mình.
             */
            employeeId =
                    currentEmployee.getId();

        }


        // =====================================================
        // ROLE KHÁC
        // =====================================================

        else {

            throw new AccessDeniedException(
                    "Bạn không có quyền xem yêu cầu cập nhật CV"
            );

        }


        Pageable pageable =
                PageRequest.of(
                        request.getPage(),
                        request.getSize()
                );


        Page<CVUpdateRequestEntity> result =
                cvUpdateRequestRepository.search(
                        employeeId,
                        request.getStatus(),
                        pageable
                );


        return result.map(
                cvUpdateRequestMapper::toResponse
        );
    }


    // =====================================================
    // GET DETAIL
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public CVUpdateRequestResponse getDetail(
            Long id
    ) {

        User currentUser =
                getCurrentUser();


        CVUpdateRequestEntity updateRequest =
                cvUpdateRequestRepository
                        .findByIdAndDeletedFalse(
                                id
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Yêu cầu cập nhật CV không tồn tại"
                                )
                        );


        Employee employee =
                updateRequest
                        .getEmployeeCV()
                        .getEmployee();


        ensureCanAccess(
                currentUser,
                employee
        );


        return cvUpdateRequestMapper.toResponse(
                updateRequest
        );
    }


    // =====================================================
    // CANCEL
    // =====================================================

    @Override
    public CVUpdateRequestResponse cancel(
            Long id
    ) {

        User currentUser =
                getCurrentUser();


        // =====================================================
        // CHỈ HR
        // =====================================================

        requireHr(
                currentUser
        );


        // =====================================================
        // FIND REQUEST
        // =====================================================

        CVUpdateRequestEntity updateRequest =
                cvUpdateRequestRepository
                        .findByIdAndDeletedFalse(
                                id
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Yêu cầu cập nhật CV không tồn tại"
                                )
                        );


        CVUpdateRequestStatus status =
                updateRequest.getStatus();


        // =====================================================
        // COMPLETED
        // =====================================================

        if (
                status ==
                        CVUpdateRequestStatus.COMPLETED
        ) {

            throw new BusinessException(
                    "error.cv.updateRequest.completedCannotCancel"
            );

        }


        // =====================================================
        // CANCELLED
        // =====================================================

        if (
                status ==
                        CVUpdateRequestStatus.CANCELLED
        ) {

            throw new BusinessException(
                    "error.cv.updateRequest.alreadyCancelled"
            );

        }


        // =====================================================
        // CANCEL
        // =====================================================

        updateRequest.setStatus(
                CVUpdateRequestStatus.CANCELLED
        );


        cvUpdateRequestRepository.save(
                updateRequest
        );


        // =====================================================
        // CV -> REQUEST_CANCELLED
        // =====================================================

        EmployeeCV employeeCV =
                updateRequest.getEmployeeCV();


        employeeCV.setStatus(
                CvStatus.REQUEST_CANCELLED
        );


        employeeCVRepository.save(
                employeeCV
        );


        return cvUpdateRequestMapper.toResponse(
                updateRequest
        );
    }


    // =====================================================
    // MARK IN PROGRESS
    // =====================================================

    @Override
    public void markInProgress(
            Long employeeCvId
    ) {

        List<CVUpdateRequestEntity> requests =
                cvUpdateRequestRepository
                        .findActiveRequests(
                                employeeCvId,
                                ACTIVE_REQUEST_STATUSES
                        );


        if (
                requests.isEmpty()
        ) {

            return;

        }


        CVUpdateRequestEntity updateRequest =
                requests.get(0);


        if (
                updateRequest.getStatus()
                        ==
                        CVUpdateRequestStatus.PENDING
        ) {

            updateRequest.setStatus(
                    CVUpdateRequestStatus.IN_PROGRESS
            );


            cvUpdateRequestRepository.save(
                    updateRequest
            );

        }

    }


    // =====================================================
    // COMPLETE
    // =====================================================

    @Override
    public void complete(
            Long employeeCvId
    ) {

        List<CVUpdateRequestEntity> requests =
                cvUpdateRequestRepository
                        .findActiveRequests(
                                employeeCvId,
                                ACTIVE_REQUEST_STATUSES
                        );


        if (
                requests.isEmpty()
        ) {

            return;

        }


        CVUpdateRequestEntity updateRequest =
                requests.get(0);


        if (
                updateRequest.getStatus()
                        ==
                        CVUpdateRequestStatus.COMPLETED
                        ||
                        updateRequest.getStatus()
                                ==
                                CVUpdateRequestStatus.CANCELLED
        ) {

            return;

        }


        updateRequest.setStatus(
                CVUpdateRequestStatus.COMPLETED
        );


        cvUpdateRequestRepository.save(
                updateRequest
        );


        // =====================================================
        // CV -> UPDATED
        // =====================================================

        EmployeeCV employeeCV =
                updateRequest.getEmployeeCV();


        employeeCV.setStatus(
                CvStatus.UPDATED
        );


        employeeCVRepository.save(
                employeeCV
        );

    }


    // =====================================================
    // CURRENT USER
    // =====================================================

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        if (
                authentication == null ||
                        !authentication.isAuthenticated() ||
                        authentication.getName() == null ||
                        authentication.getName().isBlank()
        ) {

            throw new AccessDeniedException(
                    "Người dùng chưa đăng nhập"
            );

        }


        User currentUser =
                userService.findByUsername(
                        authentication.getName()
                );


        if (
                currentUser == null ||
                        currentUser.isDeleted()
        ) {

            throw new AccessDeniedException(
                    "Tài khoản không tồn tại"
            );

        }


        return currentUser;
    }


    // =====================================================
    // REQUIRE HR
    // =====================================================

    private void requireHr(
            User currentUser
    ) {

        if (
                !hasRole(
                        currentUser,
                        RoleName.HR
                )
        ) {

            throw new AccessDeniedException(
                    "Chỉ HR được thực hiện chức năng này"
            );

        }

    }


    // =====================================================
    // CHECK ROLE
    // =====================================================

    private boolean hasRole(
            User user,
            RoleName roleName
    ) {

        return user.getUserRoles()
                .stream()
                .anyMatch(
                        userRole ->
                                userRole
                                        .getRole()
                                        .getName()
                                        .equals(
                                                roleName.name()
                                        )
                );

    }


    // =====================================================
    // CHECK ACCESS
    // =====================================================

    private void ensureCanAccess(
            User currentUser,
            Employee targetEmployee
    ) {

        if (
                targetEmployee == null ||
                        targetEmployee.isDeleted()
        ) {

            throw new ResourceNotFoundException(
                    "Nhân viên không tồn tại"
            );

        }


        // =====================================================
        // HR
        // =====================================================

        if (
                hasRole(
                        currentUser,
                        RoleName.HR
                )
        ) {

            return;

        }


        // =====================================================
        // EMPLOYEE
        // =====================================================

        if (
                hasRole(
                        currentUser,
                        RoleName.EMPLOYEE
                )
        ) {

            Employee currentEmployee =
                    currentUser.getEmployee();


            if (
                    currentEmployee == null ||
                            currentEmployee.isDeleted() ||
                            !currentEmployee
                                    .getId()
                                    .equals(
                                            targetEmployee.getId()
                                    )
            ) {

                throw new AccessDeniedException(
                        "Bạn không có quyền xem yêu cầu cập nhật CV này"
                );

            }


            return;
        }


        throw new AccessDeniedException(
                "Bạn không có quyền xem yêu cầu cập nhật CV này"
        );

    }

}