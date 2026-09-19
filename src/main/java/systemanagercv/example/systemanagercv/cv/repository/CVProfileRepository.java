package systemanagercv.example.systemanagercv.cv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import systemanagercv.example.systemanagercv.cv.entity.CVProfile;

import java.util.Optional;

public interface CVProfileRepository extends JpaRepository<CVProfile, Long> {

    Optional<CVProfile> findByCvVersionIdAndDeletedFalse(Long cvVersionId);

    boolean existsByCvVersionIdAndDeletedFalse(Long cvVersionId);
}
