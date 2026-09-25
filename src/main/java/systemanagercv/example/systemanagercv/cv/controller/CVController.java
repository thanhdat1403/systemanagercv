package systemanagercv.example.systemanagercv.cv.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import systemanagercv.example.systemanagercv.common.response.ApiResponse;
import systemanagercv.example.systemanagercv.common.security.SecurityAuthorization;
import systemanagercv.example.systemanagercv.cv.dto.request.CVCreateRequest;
import systemanagercv.example.systemanagercv.cv.dto.request.CVRejectRequest;
import systemanagercv.example.systemanagercv.cv.dto.request.CVSearchRequest;
import systemanagercv.example.systemanagercv.cv.dto.request.CVUpdateRequest;
import systemanagercv.example.systemanagercv.cv.dto.response.CVDetailResponse;
import systemanagercv.example.systemanagercv.cv.dto.response.CVListResponse;
import systemanagercv.example.systemanagercv.cv.service.CVService;

@RestController
@RequestMapping("/api/v1/cvs")
@RequiredArgsConstructor
public class CVController {

    private final CVService cvService;

    // =========================================================
    // 1. API: TÌM KIẾM + PHÂN TRANG + SẮP XẾP CV
    // GET /api/v1/cvs
    //
    // Ví dụ:
    // GET /api/v1/cvs?page=0&size=10
    // GET /api/v1/cvs?keyword=Nguyen
    // GET /api/v1/cvs?departmentId=1
    // =========================================================
    @GetMapping
    @PreAuthorize(SecurityAuthorization.CV_READER)
    public ResponseEntity<ApiResponse<Page<CVListResponse>>> search(
            @Valid @ModelAttribute CVSearchRequest request
    ) {

        Page<CVListResponse> result =
                cvService.search(request);

        ApiResponse<Page<CVListResponse>> response =
                new ApiResponse<>(
                        "success",
                        "Success",
                        result
                );

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // 2. API: XEM CHI TIẾT CV
    // GET /api/v1/cvs/{id}
    //
    // Ví dụ:
    // GET /api/v1/cvs/1
    // =========================================================
    @GetMapping("/{id}")
    @PreAuthorize(SecurityAuthorization.CV_READER)
    public ResponseEntity<ApiResponse<CVDetailResponse>> getDetail(
            @PathVariable Long id
    ) {

        CVDetailResponse result =
                cvService.getDetail(id);

        ApiResponse<CVDetailResponse> response =
                new ApiResponse<>(
                        "success",
                        "Success",
                        result
                );

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // 3. API: TẠO MỚI CV
    // POST /api/v1/cvs
    // =========================================================
    @PostMapping
    @PreAuthorize(SecurityAuthorization.CV_CREATOR)
    public ResponseEntity<ApiResponse<CVDetailResponse>> create(
            @Valid @RequestBody CVCreateRequest request
    ) {

        CVDetailResponse result =
                cvService.create(request);

        ApiResponse<CVDetailResponse> response =
                new ApiResponse<>(
                        "success",
                        "CV created successfully",
                        result
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =========================================================
    // 4. API: Cập nhật CV
    // =========================================================
    @PutMapping("/{id}")
    @PreAuthorize(SecurityAuthorization.CV_EDITOR)
    public ResponseEntity<ApiResponse<CVDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CVUpdateRequest request
    ){

        CVDetailResponse result =
                cvService.update(id, request);

        ApiResponse<CVDetailResponse> response =
                new ApiResponse<>(
                        "success",
                        "CV updated successfully",
                        result
                );
        return  ResponseEntity.ok(response);
    }

    // post api gửi bản nháp cv
    @PostMapping("/versions/{versionId}/submit")
    @PreAuthorize(SecurityAuthorization.CV_SUBMITTER)
    public ResponseEntity<ApiResponse<CVDetailResponse>> submitDraft(
            @PathVariable Long versionId
    ){

        CVDetailResponse result =
                cvService.submitDraft(versionId);

        ApiResponse<CVDetailResponse> response =
                new ApiResponse<>(
                        "success",
                        "CV draft submitted successfully",
                        result
                );

        return  ResponseEntity.ok(response);
    }

    // Hàm xử lý xóa version cv
    @PostMapping("/versions/{versionId}/cancel")
    @PreAuthorize(SecurityAuthorization.CV_CANCELLER)
    public ResponseEntity<ApiResponse<Void>> cancelDraft(
            @PathVariable Long versionId
    ){
        cvService.cancelDraft(versionId);

        return ResponseEntity.ok(
                ApiResponse.success(null)
        );

    }

    // POST API chấp nhận CV của TEACH_LEAD
    @PostMapping("/versions/{versionId}/approve")
    @PreAuthorize(SecurityAuthorization.TECH_LEAD_CV_REVIEWER)
    public ResponseEntity<ApiResponse<CVDetailResponse>> approveByTechLead(
            @PathVariable Long versionId
    ) {

        CVDetailResponse response =
                cvService.approveByTechLead(versionId);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    //POST API từ chối của TechLead
    @PostMapping("/versions/{versionId}/reject")
    @PreAuthorize(SecurityAuthorization.TECH_LEAD_CV_REVIEWER)
    public ResponseEntity<ApiResponse<CVDetailResponse>> rejectByTechLead(
            @PathVariable Long versionId,
            @Valid @RequestBody CVRejectRequest request
    ) {

        CVDetailResponse response =
                cvService.rejectByTechLead(
                        versionId,
                        request.getRejectionReason()
                );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    // POST API từ chối version cv của HR/ADMIN
    @PostMapping("/versions/{versionId}/reject-hr")
    @PreAuthorize(SecurityAuthorization.HR_CV_REVIEWER)
    public ResponseEntity<ApiResponse<CVDetailResponse>> rejectByHr(
            @PathVariable Long versionId,
            @Valid @RequestBody CVRejectRequest request
    ){

        CVDetailResponse response =
                cvService.rejectByHr(
                        versionId,
                        request.getRejectionReason()
                );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );

    }

    // POST API HR duyệt cv
    @PostMapping("/versions/{versionId}/approve-hr")
    @PreAuthorize(SecurityAuthorization.HR_CV_REVIEWER)
    public ResponseEntity<ApiResponse<CVDetailResponse>> approveByHr(
            @PathVariable Long versionId
    ){

        CVDetailResponse response =
                cvService.approveByHr(versionId);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    // API XÓA CV
    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityAuthorization.CV_DELETER)
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id
    ) {

        cvService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.success(null)
        );

    }
}
