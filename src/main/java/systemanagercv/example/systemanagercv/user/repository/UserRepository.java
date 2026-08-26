package systemanagercv.example.systemanagercv.user.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import systemanagercv.example.systemanagercv.user.entity.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {
            "userRoles",
            "userRoles.role"
    })
    User findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // =====================================================
    // LẤY USER THEO ROLE
    // =====================================================

    List<User> findByUserRoles_Role_Name(String roleName);


    // =====================================================
    // THÊM EMPLOYEE
    // =====================================================
    //
    // Chỉ lấy User:
    // - Có role EMPLOYEE
    // - Chưa có Employee
    //
    // Vì vậy:
    //
    // employee01 đã có Employee
    // → không xuất hiện
    //
    // employee02 chưa có Employee
    // → xuất hiện
    //
    // =====================================================

    @Query("""
        SELECT DISTINCT u
        FROM User u
        JOIN u.userRoles ur
        JOIN ur.role r
        LEFT JOIN u.employee e
        WHERE r.name = :roleName
          AND e.id IS NULL
        """)
    List<User> findUsersAvailableForEmployee(
            @Param("roleName") String roleName
    );


    // =====================================================
    // SỬA EMPLOYEE
    // =====================================================
    //
    // Lấy User:
    //
    // 1. Có role EMPLOYEE
    // AND
    //
    // 2. Chưa có Employee
    //
    // HOẶC
    //
    // 3. Là User hiện tại của Employee đang sửa
    //
    // =====================================================

    @Query("""
        SELECT DISTINCT u
        FROM User u
        JOIN u.userRoles ur
        JOIN ur.role r
        LEFT JOIN u.employee e
        WHERE r.name = :roleName
          AND (e.id IS NULL OR e.id = :employeeId)
        """)
    List<User> findUsersAvailableForEmployeeEdit(
            @Param("roleName") String roleName,
            @Param("employeeId") Long employeeId
    );
}