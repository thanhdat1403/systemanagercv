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
    * */
    CVDetailResponse create(CVCreateRequest cvCreateRequest);

    /**
     * Cập nhật CV hiện tại.
     */
    CVDetailResponse update(
            Long id,
            CVUpdateRequest request
    );

    /**
     * Tìm kiếm danh sách CV có phân trang.
     */
    Page<CVListResponse> search(CVSearchRequest request);

    /**
     * Lấy chi tiết CV.
     */
    CVDetailResponse getDetail(Long id);


}
