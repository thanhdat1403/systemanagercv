package systemanagercv.example.systemanagercv.cv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import systemanagercv.example.systemanagercv.cv.entity.CVExperience;

import java.util.List;

public interface CVExperienceRepository extends JpaRepository<CVExperience, Long> {

    List<CVExperience> findAllByCvVersionIdAndDeletedFalseOrderBySortOrderAsc(
            Long cvVersionId
    );
}