package systemanagercv.example.systemanagercv.cv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import systemanagercv.example.systemanagercv.cv.entity.CVProject;

import java.util.List;

public interface CVProjectRepository extends JpaRepository<CVProject, Long> {

    List<CVProject> findAllByCvVersionIdAndDeletedFalseOrderBySortOrderAsc(
            Long cvVersionId
    );
}