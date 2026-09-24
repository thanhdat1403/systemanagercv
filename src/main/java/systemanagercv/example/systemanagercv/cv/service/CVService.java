package systemanagercv.example.systemanagercv.cv.service;

import org.springframework.data.domain.Page;
import systemanagercv.example.systemanagercv.cv.dto.request.CVCreateRequest;
import systemanagercv.example.systemanagercv.cv.dto.request.CVSearchRequest;
import systemanagercv.example.systemanagercv.cv.dto.request.CVUpdateRequest;
import systemanagercv.example.systemanagercv.cv.dto.response.CVDetailResponse;
import systemanagercv.example.systemanagercv.cv.dto.response.CVListResponse;

public interface CVService {

    /*
     * Tạo CV mới cho nhân viên
     */
    CVDetailResponse create(CVCreateRequest cvCreateRequest);

    /**
     * Cập nhật CV hiện tại.
     *
     * Mỗi lần cập nhật sẽ tạo một CVVersion mới
     * ở trạng thái DRAFT.
     */
    CVDetailResponse update(
            Long id,
            CVUpdateRequest request
    );

    /**
     * Gửi bản nháp CV vào quy trình phê duyệt.
     *
     * EMPLOYEE:
     * DRAFT -> PENDING_TECH_LEAD
     *
     * TECH_LEAD:
     * DRAFT -> PENDING_HR
     */
    CVDetailResponse submitDraft(Long versionId);

    /**
     * Tech Lead duyệt CV đang chờ duyệt.
     *
     * PENDING_TECH_LEAD -> PENDING_HR
     */
    CVDetailResponse approveByTechLead(Long versionId);

    /**
     * Tech Lead từ chối CV đang chờ duyệt.
     *
     * PENDING_TECH_LEAD -> TECH_LEAD_REJECTED
     */
    CVDetailResponse rejectByTechLead(
            Long versionId,
            String rejectionReason
    );

    /**
     * ADMIN/HR từ chối CV đang chờ duyệt.
     *
     */
    CVDetailResponse rejectByHr(
            Long versionId,
            String rejectionReason
    );

    /**
     * HR chấp nhận CV.
     *
     * PENDING_HR -> HR_APPROVED -> OFFICAL
     */
    CVDetailResponse approveByHr(Long versionId);

    /**
     * Tìm kiếm danh sách CV có phân trang.
     */
    Page<CVListResponse> search(CVSearchRequest request);

    /**
     * Lấy chi tiết CV.
     */
    CVDetailResponse getDetail(Long id);
}