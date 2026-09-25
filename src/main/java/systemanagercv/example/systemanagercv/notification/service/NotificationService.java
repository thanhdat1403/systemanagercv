package systemanagercv.example.systemanagercv.notification.service;

import org.springframework.data.domain.Page;
import systemanagercv.example.systemanagercv.notification.dto.request.NotificationSearchRequest;
import systemanagercv.example.systemanagercv.notification.dto.response.NotificationResponse;
import systemanagercv.example.systemanagercv.notification.entity.Notification;
import systemanagercv.example.systemanagercv.notification.enums.NotificationType;

public interface NotificationService {

    /**
     * Tạo notification cho một User.
     */
    NotificationResponse createNotification(
            Long recipientUserId,
            NotificationType type,
            String title,
            String message,
            String referenceType,
            Long referenceId
    );

    /**
     * Lấy danh sách notification của User hiện tại.
     */
    Page<NotificationResponse> getMyNotifications(
            NotificationSearchRequest request
    );

    /**
     * Đếm số notification chưa đọc của User hiện tại.
     */
    long countMyUnread();

    /**
     * Đánh dấu một notification là đã đọc.
     */
    void markAsRead(Long notificationId);
}
/*Employee submit CV
        ↓
CVService.submitDraft()
        ↓
NotificationService.createNotification(...)
        ↓
Tech Lead nhận notification*/
