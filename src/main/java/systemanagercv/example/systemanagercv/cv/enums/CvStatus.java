package systemanagercv.example.systemanagercv.cv.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CvStatus {

    UPDATED("Đã cập nhật"),

    NOT_UPDATED("Chưa cập nhật"),

    REQUEST_CANCELLED("Hủy yêu cầu");

    private final String description;
}
