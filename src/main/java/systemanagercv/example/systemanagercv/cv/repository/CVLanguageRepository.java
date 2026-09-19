package systemanagercv.example.systemanagercv.cv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import systemanagercv.example.systemanagercv.cv.entity.CVLanguage;

import java.util.List;

public interface CVLanguageRepository extends JpaRepository<CVLanguage, Long> {

    List<CVLanguage> findAllByCvVersionIdAndDeletedFalseOrderBySortOrderAsc(
            Long cvVersionId
    );
}