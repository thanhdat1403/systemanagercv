package systemanagercv.example.systemanagercv.cv.update_request.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import systemanagercv.example.systemanagercv.cv.update_request.entity.CVUpdateRequestEntity;
import systemanagercv.example.systemanagercv.cv.update_request.enums.CVUpdateRequestStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CVUpdateRequestRepository
        extends JpaRepository<CVUpdateRequestEntity, Long> {

    // =====================================================
    // FIND BY ID
    // =====================================================

    /**
     * Lấy một Update Request đang hoạt động.
     *
     * Chỉ lấy record chưa bị soft delete.
     */
    Optional<CVUpdateRequestEntity> findByIdAndDeletedFalse(
            Long id
    );


    // =====================================================
    // LIST ALL
    // =====================================================

    /**
     * Lấy toàn bộ Update Request chưa bị soft delete.
     *
     * Mới nhất được hiển thị trước.
     */
    Page<CVUpdateRequestEntity> findAllByDeletedFalseOrderByCreatedDateDesc(
            Pageable pageable
    );


    // =====================================================
    // FIND BY EMPLOYEE CV
    // =====================================================

    /**
     * Lấy lịch sử các Update Request của một CV.
     *
     * Dùng để xem một EmployeeCV đã từng được yêu cầu
     * cập nhật bao nhiêu lần.
     */
    Page<CVUpdateRequestEntity> findAllByEmployeeCV_IdAndDeletedFalseOrderByCreatedDateDesc(
            Long employeeCvId,
            Pageable pageable
    );


    // =====================================================
    // FIND BY EMPLOYEE
    // =====================================================

    /**
     * Lấy các Update Request của một Employee.
     *
     * Quan hệ:
     *
     * CVUpdateRequest
     *      -> EmployeeCV
     *          -> Employee
     */
    Page<CVUpdateRequestEntity> findAllByEmployeeCV_Employee_IdAndDeletedFalseOrderByCreatedDateDesc(
            Long employeeId,
            Pageable pageable
    );


    // =====================================================
    // FIND BY STATUS
    // =====================================================

    /**
     * Lấy các Update Request theo trạng thái.
     */
    Page<CVUpdateRequestEntity> findAllByStatusAndDeletedFalseOrderByCreatedDateDesc(
            CVUpdateRequestStatus status,
            Pageable pageable
    );


    // =====================================================
    // FIND BY REQUESTED USER
    // =====================================================

    /**
     * Lấy các Update Request do một User tạo.
     *
     * Theo nghiệp vụ hiện tại User tạo request sẽ là HR.
     */
    Page<CVUpdateRequestEntity> findAllByRequestedBy_IdAndDeletedFalseOrderByCreatedDateDesc(
            Long userId,
            Pageable pageable
    );


    // =====================================================
    // CHECK ACTIVE REQUEST
    // =====================================================

    /**
     * Kiểm tra một CV hiện đang có Update Request
     * thuộc một trong các trạng thái đang hoạt động hay không.
     *
     * Ví dụ:
     *
     * PENDING
     * IN_PROGRESS
     *
     * được xem là request đang hoạt động.
     *
     * COMPLETED và CANCELLED không được xem là active.
     */
    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
        FROM CVUpdateRequestEntity r
        WHERE r.employeeCV.id = :employeeCvId
          AND r.deleted = false
          AND r.status IN :statuses
        """)
    boolean existsActiveRequest(
            @Param("employeeCvId") Long employeeCvId,
            @Param("statuses") Collection<CVUpdateRequestStatus> statuses
    );


    // =====================================================
    // GET ACTIVE REQUEST
    // =====================================================

    /**
     * Lấy các Update Request đang hoạt động của một CV.
     *
     * Có thể dùng khi cần kiểm tra chi tiết request hiện tại.
     */
    @Query("""
        SELECT r
        FROM CVUpdateRequestEntity r
        WHERE r.employeeCV.id = :employeeCvId
          AND r.deleted = false
          AND r.status IN :statuses
        ORDER BY r.createdDate DESC
        """)
    List<CVUpdateRequestEntity> findActiveRequests(
            @Param("employeeCvId") Long employeeCvId,
            @Param("statuses") Collection<CVUpdateRequestStatus> statuses
    );

    // =====================================================
    // SEARCH WITH OPTIONAL FILTERS
    // =====================================================

        /**
         * Tìm kiếm Update Request với các điều kiện tùy chọn:
         *
         * employeeId = null → không lọc Employee
         * status = null      → không lọc Status
         *
         * Có thể kết hợp cả hai.
         */
        @Query("""
        SELECT r
        FROM CVUpdateRequestEntity r
        WHERE r.deleted = false
          AND (:employeeId IS NULL
               OR r.employeeCV.employee.id = :employeeId)
          AND (:status IS NULL
               OR r.status = :status)
        ORDER BY r.createdDate DESC
        """)
        Page<CVUpdateRequestEntity> search(
                @Param("employeeId") Long employeeId,
                @Param("status") CVUpdateRequestStatus status,
                Pageable pageable
        );
}