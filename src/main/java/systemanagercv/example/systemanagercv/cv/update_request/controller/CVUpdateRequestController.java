package systemanagercv.example.systemanagercv.cv.update_request.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import systemanagercv.example.systemanagercv.common.response.ApiResponse;
import systemanagercv.example.systemanagercv.common.security.SecurityAuthorization;
import systemanagercv.example.systemanagercv.cv.update_request.dto.request.CVUpdateRequestCreateRequest;
import systemanagercv.example.systemanagercv.cv.update_request.dto.request.CVUpdateRequestSearchRequest;
import systemanagercv.example.systemanagercv.cv.update_request.dto.response.CVUpdateRequestResponse;
import systemanagercv.example.systemanagercv.cv.update_request.repository.CVUpdateRequestRepository;
import systemanagercv.example.systemanagercv.cv.update_request.service.CVUpdateRequestService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cv-update-requests")
@RequiredArgsConstructor
public class CVUpdateRequestController {

    private final CVUpdateRequestService cvUpdateRequestService;

    // =====================================================
    // CREATE
    // =====================================================

    /**
     * HR tạo yêu cầu cập nhật CV cho một Employee.
     *
     * POST /api/v1/cv-update-requests
     *
     * Chỉ HR được phép gọi.
     */
    @PostMapping
    @PreAuthorize(
            SecurityAuthorization.UPDATE_REQUEST_CREATOR
    )
    public ResponseEntity<
            ApiResponse<List<CVUpdateRequestResponse>>> create(
            @Valid @RequestBody
            CVUpdateRequestCreateRequest request
    ) {

        List<CVUpdateRequestResponse> result =
                cvUpdateRequestService.createBatch(
                        request
                );

        ApiResponse<List<CVUpdateRequestResponse>> response =
                new ApiResponse<>(
                        "success",
                        "Tạo yêu cầu cập nhật CV thành công",
                        result
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =====================================================
    // SEARCH
    // =====================================================

    /**
     * Lấy danh sách Update Request.
     *
     * GET /api/v1/cv-update-requests
     *
     * Ví dụ:
     *
     * GET /api/v1/cv-update-requests
     *
     * GET /api/v1/cv-update-requests?page=0&size=10
     *
     * GET /api/v1/cv-update-requests?status=PENDING
     *
     * GET /api/v1/cv-update-requests?employeeId=5
     *
     * HR:
     *      Có thể xem toàn bộ.
     *
     * EMPLOYEE:
     *      Service tự ép employeeId
     *      về Employee hiện tại.
     */
    @GetMapping
    @PreAuthorize(
            SecurityAuthorization.UPDATE_REQUEST_READER
    )
    public ResponseEntity<
            ApiResponse<Page<CVUpdateRequestResponse>>
            > search(
            @Valid @ModelAttribute
            CVUpdateRequestSearchRequest request
    ) {

        Page<CVUpdateRequestResponse> result =
                cvUpdateRequestService.search(
                        request
                );

        ApiResponse<Page<CVUpdateRequestResponse>> response =
                new ApiResponse<>(
                        "success",
                        "Lấy danh sách yêu cầu cập nhật CV thành công",
                        result
                );

        return ResponseEntity.ok(
                response
        );
    }

    // =====================================================
    // DETAIL
    // =====================================================

    /**
     * Lấy chi tiết một Update Request.
     *
     * GET /api/v1/cv-update-requests/{id}
     *
     * HR:
     *      Có thể xem.
     *
     * EMPLOYEE:
     *      Chỉ xem request của chính mình.
     */
    @GetMapping("/{id}")
    @PreAuthorize(
            SecurityAuthorization.UPDATE_REQUEST_READER
    )
    public ResponseEntity<
            ApiResponse<CVUpdateRequestResponse>
            > getDetail(
            @PathVariable Long id
    ) {

        CVUpdateRequestResponse result =
                cvUpdateRequestService.getDetail(
                        id
                );

        ApiResponse<CVUpdateRequestResponse> response =
                new ApiResponse<>(
                        "success",
                        "Lấy chi tiết yêu cầu cập nhật CV thành công",
                        result
                );

        return ResponseEntity.ok(
                response
        );
    }

    // =====================================================
    // CANCEL
    // =====================================================

    /**
     * HR hủy Update Request.
     *
     * POST /api/v1/cv-update-requests/{id}/cancel
     *
     * Khi thành công:
     *
     * Request
     *      -> CANCELLED
     *
     * EmployeeCV
     *      -> REQUEST_CANCELLED
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize(
            SecurityAuthorization.UPDATE_REQUEST_CANCELLER
    )
    public ResponseEntity<
            ApiResponse<CVUpdateRequestResponse>
            > cancel(
            @PathVariable Long id
    ) {

        CVUpdateRequestResponse result =
                cvUpdateRequestService.cancel(
                        id
                );

        ApiResponse<CVUpdateRequestResponse> response =
                new ApiResponse<>(
                        "success",
                        "Hủy yêu cầu cập nhật CV thành công",
                        result
                );

        return ResponseEntity.ok(
                response
        );
    }
}
