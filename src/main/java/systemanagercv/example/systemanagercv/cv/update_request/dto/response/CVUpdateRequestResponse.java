package systemanagercv.example.systemanagercv.cv.update_request.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import systemanagercv.example.systemanagercv.cv.update_request.enums.CVUpdateRequestStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVUpdateRequestResponse {

    // =====================================================
    // UPDATE REQUEST
    // =====================================================

    /**
     * ID của yêu cầu cập nhật CV.
     */
    private Long id;

    /**
     * ID của EmployeeCV được yêu cầu cập nhật.
     */
    private Long employeeCvId;

    // =====================================================
    // EMPLOYEE
    // =====================================================

    /**
     * ID của Employee sở hữu CV.
     */
    private Long employeeId;

    /**
     * Mã nhân viên.
     */
    private String employeeCode;

    /**
     * Họ và tên nhân viên.
     */
    private String employeeName;

    /**
     * Mã phòng ban.
     */
    private String departmentCode;

    /**
     * Tên phòng ban.
     */
    private String departmentName;

    // =====================================================
    // REQUESTED BY
    // =====================================================

    /**
     * ID User đã tạo yêu cầu.
     */
    private Long requestedByUserId;

    /**
     * Username của User đã tạo yêu cầu.
     */
    private String requestedByUsername;

    // =====================================================
    // STATUS
    // =====================================================

    /**
     * Trạng thái hiện tại của yêu cầu.
     */
    private CVUpdateRequestStatus status;

    // =====================================================
    // AUDIT
    // =====================================================

    /**
     * Thời điểm tạo yêu cầu.
     */
    private LocalDateTime createdDate;

    /**
     * Thời điểm cập nhật request gần nhất.
     */
    private LocalDateTime updatedDate;

    /**
     * Nội dung yêu cầu cập nhật CV
     */
    private String content;

    /**
     * Thời hạn để xử lý cập nhật CV theo yêu cầu
     */
    private LocalDate deadline;
}