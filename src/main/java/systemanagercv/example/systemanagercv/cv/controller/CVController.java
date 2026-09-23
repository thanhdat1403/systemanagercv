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
}
