package systemanagercv.example.systemanagercv.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import systemanagercv.example.systemanagercv.employee.entity.Employee;

import java.util.Optional;

public interface EmployeeRepository
        extends JpaRepository<Employee, Long>,
        JpaSpecificationExecutor<Employee> {

    boolean existsByEmployeeCodeAndDeletedFalse(String employeeCode);

    boolean existsByEmployeeCodeAndIdNotAndDeletedFalse(
            String employeeCode,
            Long id
    );

    boolean existsByUserIdAndDeletedFalse(Long userId);

    @Query("""
        SELECT e
        FROM Employee e
        JOIN FETCH e.user u
        JOIN FETCH e.department d
        WHERE e.id = :id
          AND e.deleted = false
        """)
    Optional<Employee> findEmployeeDetailById(
            @Param("id") Long id
    );
}