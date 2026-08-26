package systemanagercv.example.systemanagercv.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import systemanagercv.example.systemanagercv.entity.Departments;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Departments, Long> {
    //Viết hàm này để kiểm tra trùng mã phòng ban khi thêm mới/cập nhật
    Optional<Departments> findByCode(String code);

    //Tìm kiếm
    @Query("""
        SELECT d
        FROM Departments d
        WHERE LOWER(d.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    Page<Departments> searchDepartment(
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
