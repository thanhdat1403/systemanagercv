package systemanagercv.example.systemanagercv.cv.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import systemanagercv.example.systemanagercv.cv.entity.EmployeeCV;
import systemanagercv.example.systemanagercv.cv.projection.CVListProjection;
import systemanagercv.example.systemanagercv.cv.enums.CvStatus;
import java.util.Optional;

public interface EmployeeCVRepository extends JpaRepository<EmployeeCV, Long> {

    Optional<EmployeeCV> findByEmployeeIdAndDeletedFalse(Long employeeId);

    boolean existsByEmployeeIdAndDeletedFalse(Long employeeId);

    @Query("""
        SELECT cv
        FROM EmployeeCV cv
        JOIN FETCH cv.employee e
        JOIN FETCH e.department d
        WHERE cv.id = :id
          AND cv.deleted = false
          AND e.deleted = false
          AND d.deleted = false
        """)
    Optional<EmployeeCV> findActiveById(@Param("id") Long id);

    /**
     * Tìm kiếm danh sách CV có:
     * - keyword
     * - department
     * - CV status
     * - authorization scope
     * - pagination
     *
     * Scope:
     * - scopeDepartmentId != null → chỉ CV trong phòng ban đó
     * - scopeEmployeeId != null → chỉ CV của employee đó
     * - cả hai null → xem toàn bộ CV
     */
    @Query(
            value = """
                    SELECT
                        cv.id AS id,
                        e.id AS employeeId,
                        e.employeeCode AS employeeCode,
                        e.fullName AS employeeName,
                        d.code AS departmentCode,
                        d.name AS departmentName,
                        cv.status AS status,
                        v.version AS currentVersion
                    FROM EmployeeCV cv
                    JOIN cv.employee e
                    JOIN e.department d
                    LEFT JOIN cv.currentVersion v
                    WHERE cv.deleted = false
                      AND e.deleted = false
                      AND d.deleted = false

                    /* Tìm kiếm theo từ khóa (:keyword)*/
                      AND (
                          :keyword IS NULL
                          OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                          OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                      )
                    /* Lọc theo phòng ban chủ động (:departmentId)
                    Khi ng dùng chọn một phòng ban cụ thể trên thanh Tìm kiếm, hệ thống sẽ chỉ lọc ra những CV thuộc phòng ban đó */
                      AND (
                          :departmentId IS NULL
                          OR d.id = :departmentId
                      )
                    /* Lọc theo trạng thái CV (:status)
                    Lọc CV theo trạng thái (ví dụ: chỉ xem các CV dạng ĐANG_CHỜ_DUYỆT, hoặc ĐÃ_DUYỆT)
                    . Biến :status này rất có thể là một Enum mà ta đã set*/
                      AND (
                          :status IS NULL
                          OR cv.status = :status
                      )
                    /* Phân quyền xem dữ liệu (Scope)
                    TH1:(Bn là ADMIN toàn quyền) Service sẽ truyền cả ::scopeDepartmentId và :scopeEmployeeId bằng NULL.
                    Lúc này cả 2 điều kiện scope đều chuyển thành true OR... -> Bộ lọc biến mất. Admin sẽ nhìn thấy toàn bộ CV của cty
                    TH2:(Bn là Manager) Hệ thống sẽ lấy ID phòng ban của vị trưởng phòng này truyền vào :scopeDepartmentId, còn :scopeEmployeeId để NULL
                    Lúc này câu lệnh ép buộc d.id = :scopeDepartmentId. Trưởng phòng chỉ được quyền nhìn thấy CV của các nhân viên trong phòng mình quản lý*/
                      AND (
                          :scopeDepartmentId IS NULL
                          OR d.id = :scopeDepartmentId
                      )
                    /*TH3:(Bn là Employee) Hệ thống sẽ lấy chính ID của nhân viên này truyền vào :scopeEmployeeId, còn :scopeDepartmentId để NULL
                    Lúc này câu lệnh ép buộc e.id = :scopeEmployeeId. Nhân viên thường chỉ nhìn thấy duy nhất 1 chiếc CV của chính mình, không xem được của người khác.*/
                      AND (
                          :scopeEmployeeId IS NULL
                          OR e.id = :scopeEmployeeId
                      )
                    """,
            /*Dùng cho phân trang , câu lệnh SELECT ở trên để lấy đúng 10 dòng dữ liệu
            * countQuery để đếm tổng số lượng dòng thỏa mãn điều kiện lọc*/
            countQuery = """
                    SELECT COUNT(cv.id)
                    FROM EmployeeCV cv
                    JOIN cv.employee e
                    JOIN e.department d
                    WHERE cv.deleted = false
                      AND e.deleted = false
                      AND d.deleted = false

                      AND (
                          :keyword IS NULL
                          OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                          OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                      )

                      AND (
                          :departmentId IS NULL
                          OR d.id = :departmentId
                      )

                      AND (
                          :status IS NULL
                          OR cv.status = :status
                      )

                      AND (
                          :scopeDepartmentId IS NULL
                          OR d.id = :scopeDepartmentId
                      )

                      AND (
                          :scopeEmployeeId IS NULL
                          OR e.id = :scopeEmployeeId
                      )
                    """
    )
    Page<CVListProjection> searchActiveForList(
            @Param("keyword") String keyword,
            @Param("departmentId") Long departmentId,
            @Param("status") CvStatus status,
            @Param("scopeDepartmentId") Long scopeDepartmentId,
            @Param("scopeEmployeeId") Long scopeEmployeeId,
            Pageable pageable
    );
}