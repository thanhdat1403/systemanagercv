package systemanagercv.example.systemanagercv.profile.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import systemanagercv.example.systemanagercv.common.entity.BaseEntity;
import systemanagercv.example.systemanagercv.profile.enums.AvatarChangeRequestStatus;
import systemanagercv.example.systemanagercv.user.entity.User;

@Entity
@Table(
        name = "avatar_change_requests"
)
@Getter
@Setter
@NoArgsConstructor
public class AvatarChangeRequest extends BaseEntity {

    // =====================================================
    // USER GỬI YÊU CẦU
    // =====================================================

    /**
     * User thực hiện yêu cầu thay đổi avatar.
     *
     * Mapping:
     *
     * avatar_change_requests.user_id
     *              ↓
     *          users.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_avatar_change_requests_user"
            )
    )
    private User user;


    // =====================================================
    // PENDING OBJECT KEY
    // =====================================================

    /**
     * Object key của avatar đang chờ Admin duyệt.
     *
     * Ví dụ:
     *
     * profile/avatar/pending/5/2026/10/abc.jpg
     */
    @Column(
            name = "pending_object_key",
            nullable = false,
            length = 500
    )
    private String pendingObjectKey;


    // =====================================================
    // STATUS
    // =====================================================

    /**
     * Trạng thái yêu cầu:
     *
     * PENDING
     * APPROVED
     * REJECTED
     *
     * Database lưu String,
     * không lưu số thứ tự của Enum.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private AvatarChangeRequestStatus status =
            AvatarChangeRequestStatus.PENDING;


    // =====================================================
    // NGƯỜI DUYỆT / TỪ CHỐI
    // =====================================================

    /**
     * User thực hiện duyệt hoặc từ chối request.
     *
     * Khi request mới tạo:
     *
     * reviewedBy = null
     *
     * Sau khi Admin xử lý:
     *
     * reviewedBy = User của Admin
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "reviewed_by",
            foreignKey = @ForeignKey(
                    name = "fk_avatar_change_requests_reviewer"
            )
    )
    private User reviewedBy;


    // =====================================================
    // LÝ DO TỪ CHỐI
    // =====================================================

    /**
     * Lý do Admin từ chối avatar.
     *
     * Chỉ có giá trị khi:
     *
     * status = REJECTED
     */
    @Column(
            name = "rejection_reason",
            length = 1000
    )
    private String rejectionReason;
}