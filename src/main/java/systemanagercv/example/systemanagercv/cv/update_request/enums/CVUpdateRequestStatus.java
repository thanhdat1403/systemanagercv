package systemanagercv.example.systemanagercv.cv.update_request.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CVUpdateRequestStatus {

    /**
     * HR đã tạo yêu cầu.
     * Employee chưa bắt đầu cập nhật CV.
     */
    PENDING(
            "Chờ cập nhật"
    ),

    /**
     * Employee đã bắt đầu thực hiện cập nhật CV.
     * Thường được xác định khi Employee tạo Version DRAFT từ yêu cầu.
     */
    IN_PROGRESS(
            "Đang cập nhật"
    ),

    /**
     * Yêu cầu đã hoàn thành.
     * CV đã đi qua workflow và có Version OFFICIAL mới.
     */
    COMPLETED(
            "Đã hoàn thành"
    ),

    /**
     * HR đã hủy yêu cầu cập nhật.
     */
    CANCELLED(
            "Đã hủy"
    );

    private final String description;
}
