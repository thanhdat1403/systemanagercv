package systemanagercv.example.systemanagercv.cv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import systemanagercv.example.systemanagercv.cv.entity.CVSkill;

import java.util.List;

public interface CVSkillRepository extends JpaRepository<CVSkill, Long> {

    List<CVSkill> findAllByCvVersionIdAndDeletedFalseOrderBySortOrderAsc(
            Long cvVersionId
    );
}
