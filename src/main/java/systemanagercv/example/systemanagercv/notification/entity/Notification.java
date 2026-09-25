package systemanagercv.example.systemanagercv.notification.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import systemanagercv.example.systemanagercv.common.entity.BaseEntity;
import systemanagercv.example.systemanagercv.notification.enums.NotificationType;
import systemanagercv.example.systemanagercv.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class Notification extends BaseEntity {

    /**
     * User nhận notification.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "recipient_user_id",
            nullable = false
    )
    private User recipient;

    /**
     * Loại notification.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "type",
            nullable = false,
            length = 50
    )
    private NotificationType type;

    /**
     * Tiêu đề notification.
     */
    @Column(
            name = "title",
            nullable = false,
            length = 255
    )
    private String title;

    /**
     * Nội dung notification.
     */
    @Column(
            name = "message",
            nullable = false,
            length = 2000
    )
    private String message;

    /**
     * Loại đối tượng mà notification tham chiếu.
     *
     * Ví dụ:
     * CV_VERSION
     */
    @Column(
            name = "reference_type",
            length = 50
    )
    private String referenceType;

    /**
     * ID của đối tượng được tham chiếu.
     *
     * Ví dụ:
     * CVVersion ID = 20
     */
    @Column(
            name = "reference_id"
    )
    private Long referenceId;

    /**
     * Đã đọc notification hay chưa.
     */
    @Column(
            name = "is_read",
            nullable = false
    )
    private Boolean isRead = false;

    /**
     * Thời điểm notification được đọc.
     */
    @Column(
            name = "read_at"
    )
    private LocalDateTime readAt;
}