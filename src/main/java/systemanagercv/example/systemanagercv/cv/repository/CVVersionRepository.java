package systemanagercv.example.systemanagercv.cv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import systemanagercv.example.systemanagercv.cv.entity.CVVersion;
import systemanagercv.example.systemanagercv.cv.projection.CVVersionProjection;

import java.util.List;
import java.util.Optional;

public interface CVVersionRepository extends JpaRepository<CVVersion, Long> {

    // =========================================================
    // SUBMIT / GET VERSION DETAIL
    // =========================================================

    Optional<CVVersion> findByIdAndDeletedFalse(Long id);

    // =========================================================
    // VERSION HISTORY
    // =========================================================

    List<CVVersion> findAllByEmployeeCVIdAndDeletedFalseOrderByCreatedDateDesc(
            Long employeeCvId
    );

    // =========================================================
    // FIND VERSION BY VERSION NUMBER
    // =========================================================

    Optional<CVVersion> findByEmployeeCVIdAndVersionAndDeletedFalse(
            Long employeeCvId,
            String version
    );

    // =========================================================
    // FIND CURRENT OFFICIAL VERSION
    // =========================================================

    Optional<CVVersion> findByEmployeeCVIdAndIsCurrentTrueAndDeletedFalse(
            Long employeeCvId
    );

    // =========================================================
    // CHECK DUPLICATE VERSION
    // =========================================================

    boolean existsByEmployeeCVIdAndVersionAndDeletedFalse(
            Long employeeCvId,
            String version
    );

    @Query("""
        SELECT v.version
        FROM CVVersion v
        WHERE v.employeeCV.id = :employeeCvId
          AND v.deleted = false
        """)
    List<String> findAllVersionNumbers(
            @Param("employeeCvId") Long employeeCvId
    );

    // =========================================================
    // VERSION HISTORY PROJECTION
    // =========================================================

    @Query("""
            SELECT
                v.id AS id,
                cv.id AS employeeCvId,
                v.version AS version,
                v.status AS status,
                v.isCurrent AS current
            FROM CVVersion v
            JOIN v.employeeCV cv
            WHERE cv.id = :employeeCvId
              AND v.deleted = false
              AND cv.deleted = false
            ORDER BY v.createdDate DESC
            """)
    List<CVVersionProjection> findAllActiveProjections(
            @Param("employeeCvId") Long employeeCvId
    );
}