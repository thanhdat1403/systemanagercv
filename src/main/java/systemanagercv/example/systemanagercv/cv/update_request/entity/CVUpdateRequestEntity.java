package systemanagercv.example.systemanagercv.cv.update_request.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import systemanagercv.example.systemanagercv.common.entity.BaseEntity;
import systemanagercv.example.systemanagercv.cv.entity.EmployeeCV;
import systemanagercv.example.systemanagercv.cv.update_request.enums.CVUpdateRequestStatus;
import systemanagercv.example.systemanagercv.user.entity.User;

import java.time.LocalDate;
@Entity
@Table(name = "cv_update_requests")
@Getter
@Setter
@NoArgsConstructor
public class CVUpdateRequestEntity extends BaseEntity {

    // =====================================================
    // CV ĐƯỢC YÊU CẦU CẬP NHẬT
    // =====================================================

    /**
     * CV mà HR yêu cầu Employee cập nhật.
     *
     * Một EmployeeCV có thể có nhiều request
     * theo thời gian vì các request cũ vẫn được lưu lại
     * để phục vụ lịch sử.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "employee_cv_id",
            nullable = false
    )
    private EmployeeCV employeeCV;

    // =====================================================
    // NGƯỜI TẠO YÊU CẦU
    // =====================================================

    /**
     * User đã tạo yêu cầu cập nhật CV.
     *
     * Theo nghiệp vụ hiện tại:
     * HR là người tạo Update Request.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "requested_by",
            nullable = false
    )
    private User requestedBy;

    // =====================================================
    // TRẠNG THÁI YÊU CẦU
    // =====================================================

    /**
     * Trạng thái vòng đời của yêu cầu cập nhật CV.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private CVUpdateRequestStatus status =
            CVUpdateRequestStatus.PENDING;

    // =====================================================
    // NỘI DUNG YÊU CẦU
    // =====================================================

    @Column(
            name = "content",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String content;

    // =====================================================
    // DEADLINE
    // =====================================================

    @Column(
            name = "deadline",
            nullable = false
    )
    private LocalDate deadline;
}