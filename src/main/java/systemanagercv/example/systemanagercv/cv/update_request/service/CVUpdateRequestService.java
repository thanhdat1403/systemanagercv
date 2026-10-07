package systemanagercv.example.systemanagercv.cv.update_request.service;

import org.springframework.data.domain.Page;
import systemanagercv.example.systemanagercv.cv.update_request.dto.request.CVUpdateRequestCreateRequest;
import systemanagercv.example.systemanagercv.cv.update_request.dto.request.CVUpdateRequestSearchRequest;
import systemanagercv.example.systemanagercv.cv.update_request.dto.response.CVUpdateRequestResponse;

import java.util.List;

public interface CVUpdateRequestService {

    /**
     * =====================================================
     * CREATE
     * =====================================================
     *
     * HR tạo yêu cầu cho một Employee cập nhật CV.
     *
     * Service sẽ thực hiện:
     *
     * 1. Kiểm tra quyền người tạo.
     * 2. Tìm EmployeeCV.
     * 3. Kiểm tra request đang hoạt động.
     * 4. Tạo CVUpdateRequestEntity.
     * 5. Đặt status = PENDING.
     * 6. Đặt EmployeeCV.status = NOT_UPDATED.
     * 7. Tạo notification cho Employee.
     */
    List<CVUpdateRequestResponse> createBatch(
            CVUpdateRequestCreateRequest request
    );


    /**
     * =====================================================
     * SEARCH
     * =====================================================
     *
     * Lấy danh sách Update Request có phân trang.
     *
     * Service sẽ dựa vào User hiện tại để xác định
     * phạm vi dữ liệu được phép xem.
     */
    Page<CVUpdateRequestResponse> search(
            CVUpdateRequestSearchRequest request
    );


    /**
     * =====================================================
     * GET DETAIL
     * =====================================================
     *
     * Lấy chi tiết một Update Request.
     */
    CVUpdateRequestResponse getDetail(
            Long id
    );


    /**
     * =====================================================
     * CANCEL
     * =====================================================
     *
     * Hủy một Update Request.
     *
     * Service sẽ kiểm tra:
     *
     * - Request có tồn tại hay không.
     * - Người thực hiện có quyền hủy hay không.
     * - Request đã COMPLETED / CANCELLED hay chưa.
     *
     * Khi hủy:
     *
     * Request status
     *      -> CANCELLED
     *
     * EmployeeCV.status
     *      -> REQUEST_CANCELLED
     */
    CVUpdateRequestResponse cancel(
            Long id
    );


    /**
     * =====================================================
     * MARK IN PROGRESS
     * =====================================================
     *
     * Đưa request từ PENDING sang IN_PROGRESS.
     *
     * Method này được Service CV sử dụng khi Employee
     * bắt đầu thực hiện cập nhật CV theo một Update Request.
     *
     * Không phải endpoint public dành cho frontend.
     */
    void markInProgress(
            Long employeeCvId
    );


    /**
     * =====================================================
     * COMPLETE
     * =====================================================
     *
     * Đánh dấu Update Request đã hoàn thành.
     *
     * Dùng khi Version mới của CV đã được HR phê duyệt
     * và trở thành OFFICIAL.
     *
     * Request:
     *      -> COMPLETED
     *
     * EmployeeCV:
     *      -> UPDATED
     *
     * Không phải endpoint public dành cho frontend.
     */
    void complete(
            Long employeeCvId
    );
}