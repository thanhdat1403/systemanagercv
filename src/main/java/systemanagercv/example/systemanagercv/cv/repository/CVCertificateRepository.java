package systemanagercv.example.systemanagercv.cv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import systemanagercv.example.systemanagercv.cv.entity.CVCertificate;

import java.util.List;

public interface CVCertificateRepository extends JpaRepository<CVCertificate, Long> {

    List<CVCertificate> findAllByCvVersionIdAndDeletedFalseOrderBySortOrderAsc(
            Long cvVersionId
    );
}