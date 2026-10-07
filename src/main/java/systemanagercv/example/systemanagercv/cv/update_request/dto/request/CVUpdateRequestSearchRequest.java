package systemanagercv.example.systemanagercv.cv.update_request.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import systemanagercv.example.systemanagercv.cv.update_request.enums.CVUpdateRequestStatus;

@Getter
@Setter
public class CVUpdateRequestSearchRequest {

    /**
     * Số trang.
     *
     * Spring Data bắt đầu từ page = 0.
     */
    @Min(
            value = 0,
            message = "Page phải lớn hơn hoặc bằng 0"
    )
    private int page = 0;

    /**
     * Số bản ghi trên mỗi trang.
     */
    @Min(
            value = 1,
            message = "Size phải lớn hơn hoặc bằng 1"
    )
    @Max(
            value = 100,
            message = "Size không được lớn hơn 100"
    )
    private int size = 10;

    /**
     * Lọc theo trạng thái của Update Request.
     *
     * Không bắt buộc.
     *
     * Ví dụ:
     *
     * ?status=PENDING
     * ?status=IN_PROGRESS
     * ?status=COMPLETED
     * ?status=CANCELLED
     */
    private CVUpdateRequestStatus status;

    /**
     * Lọc các request của một Employee cụ thể.
     *
     * Không bắt buộc.
     */
    private Long employeeId;
}