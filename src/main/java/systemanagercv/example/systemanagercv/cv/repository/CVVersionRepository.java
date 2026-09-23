package systemanagercv.example.systemanagercv.cv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import systemanagercv.example.systemanagercv.cv.entity.CVVersion;
import systemanagercv.example.systemanagercv.cv.projection.CVVersionProjection;

import java.util.List;
import java.util.Optional;

public interface CVVersionRepository extends JpaRepository<CVVersion, Long> {

    // Lấy toàn bộ version -> sau này có thể dùng cho chức năng xem lịch sử version
    List<CVVersion> findAllByEmployeeCVIdAndDeletedFalseOrderByCreatedDateDesc(
            Long employeeCvId
    );

    Optional<CVVersion> findByEmployeeCVIdAndVersionAndDeletedFalse(
            Long employeeCvId,
            String version
    );

    //Lấy Version hiện tại chưa bị xóa và sẽ dùng trong update()
    Optional<CVVersion> findByEmployeeCVIdAndIsCurrentTrueAndDeletedFalse(
            Long employeeCvId
    );

    //Kiểm tra version đã tồn tại chưa dùng để tránh tạo trùng v1.1
    boolean existsByEmployeeCVIdAndVersionAndDeletedFalse(
            Long employeeCvId,
            String version
    );

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
    //Lấy projection → sau này phục vụ API lịch sử version.
    List<CVVersionProjection> findAllActiveProjections(
            @Param("employeeCvId") Long employeeCvId
    );
}