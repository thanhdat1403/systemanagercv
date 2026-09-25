package systemanagercv.example.systemanagercv.notification.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    CV_SUBMITTED(
            "CV được gửi duyệt"
    ),

    CV_TECH_LEAD_APPROVED(
            "CV đã được Tech Lead phê duyệt"
    ),

    CV_TECH_LEAD_REJECTED(
            "CV bị Tech Lead từ chối"
    ),

    CV_HR_APPROVED(
            "CV đã được HR phê duyệt"
    ),

    CV_HR_REJECTED(
            "CV bị HR từ chối"
    );

    private final String description;
}
