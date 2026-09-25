package systemanagercv.example.systemanagercv.notification.dto.response;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import systemanagercv.example.systemanagercv.notification.enums.NotificationType;

import java.time.LocalDateTime;


@Getter
@Setter
@Builder
public class NotificationResponse {

    private Long id;

    private NotificationType type;

    private String typeDescription;

    private String title;

    private String message;

    private String referenceType;

    private Long referenceId;

    private Boolean isRead;

    private LocalDateTime readAt;

    private LocalDateTime createdDate;
}
/*{
    "id": 1,
    "type": "CV_SUBMITTED",
    "typeDescription": "CV được gửi duyệt",
    "title": "CV mới cần phê duyệt",
    "message": "CV của Nguyễn Văn A đã được gửi để bạn phê duyệt.",
    "referenceType": "CV_VERSION",
    "referenceId": 18,
    "isRead": false,
    "readAt": null,
    "createdDate": "2026-09-25T13:40:00"
}*/