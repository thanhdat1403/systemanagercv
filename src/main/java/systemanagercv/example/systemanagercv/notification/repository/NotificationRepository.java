package systemanagercv.example.systemanagercv.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import systemanagercv.example.systemanagercv.notification.entity.Notification;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    /**
     * Lấy notification đang active theo ID.
     */
    Optional<Notification> findByIdAndDeletedFalse(Long id);

    /**
     * Lấy danh sách notification của một User.
     *
     * Chỉ lấy notification chưa bị soft delete.
     * Notification mới nhất được hiển thị trước.
     */
    Page<Notification> findAllByRecipientIdAndDeletedFalseOrderByCreatedDateDesc(
            Long recipientUserId,
            Pageable pageable
    );

    /**
     * Lấy danh sách notification chưa đọc của một User.
     */
    Page<Notification> findAllByRecipientIdAndIsReadFalseAndDeletedFalseOrderByCreatedDateDesc(
            Long recipientUserId,
            Pageable pageable
    );

    /**
     * Đếm số notification chưa đọc của một User.
     */
    long countByRecipientIdAndIsReadFalseAndDeletedFalse(
            Long recipientUserId
    );

    /**
     * Đánh dấu notification là đã đọc.
     */
    @Modifying
    @Query("""
        UPDATE Notification n
        SET n.isRead = true,
            n.readAt = :readAt
        WHERE n.id = :notificationId
          AND n.recipient.id = :recipientUserId
          AND n.deleted = false
          AND n.isRead = false
        """)
    int markAsRead(
            @Param("notificationId") Long notificationId,
            @Param("recipientUserId") Long recipientUserId,
            @Param("readAt") LocalDateTime readAt
    );
}
