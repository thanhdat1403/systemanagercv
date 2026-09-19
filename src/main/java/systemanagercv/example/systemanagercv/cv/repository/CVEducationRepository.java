package systemanagercv.example.systemanagercv.cv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import systemanagercv.example.systemanagercv.cv.entity.CVEducation;

import java.util.List;

public interface CVEducationRepository extends JpaRepository<CVEducation, Long> {

    List<CVEducation> findAllByCvVersionIdAndDeletedFalseOrderBySortOrderAsc(
            Long cvVersionId
    );
}
