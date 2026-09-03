package systemanagercv.example.systemanagercv.user.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import systemanagercv.example.systemanagercv.user.entity.User;
import systemanagercv.example.systemanagercv.user.projection.UserListProjection;

import java.util.List;

public interface UserRepository
        extends JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    // =====================================================
    // TÌM USER THEO USERNAME
    // =====================================================

    @EntityGraph(attributePaths = {
            "userRoles",
            "userRoles.role"
    })
    User findByUsername(String username);


    // =====================================================
    // KIỂM TRA USERNAME / EMAIL
    // =====================================================

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
    // - Chưa bị xóa mềm
    //
    // =====================================================

    @Query("""
        SELECT DISTINCT u
        FROM User u
        JOIN u.userRoles ur
        JOIN ur.role r
        LEFT JOIN u.employee e
        WHERE u.deleted = false
          AND r.name = :roleName
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
    // - Có role EMPLOYEE
    // - Chưa bị xóa mềm
    // - Và:
    //      + Chưa có Employee
    //      HOẶC
    //      + Là User hiện tại của Employee đang sửa
    //
    // =====================================================

    @Query("""
        SELECT DISTINCT u
        FROM User u
        JOIN u.userRoles ur
        JOIN ur.role r
        LEFT JOIN u.employee e
        WHERE u.deleted = false
          AND r.name = :roleName
          AND (e.id IS NULL OR e.id = :employeeId)
        """)
    List<User> findUsersAvailableForEmployeeEdit(
            @Param("roleName") String roleName,
            @Param("employeeId") Long employeeId
    );


    // =====================================================
    // LẤY USER LIST PROJECTION
    // =====================================================
    //
    // Chỉ lấy những field cần thiết cho màn hình danh sách:
    // - id
    // - username
    // - email
    // - enabled
    // - roleId
    // - roleName
    // - roleDescription
    //
    // Không lấy password và các quan hệ không cần thiết.
    //
    // =====================================================

    @Query("""
        SELECT
            u.id AS id,
            u.username AS username,
            u.email AS email,
            u.enabled AS enabled,
            r.id AS roleId,
            r.name AS roleName,
            r.description AS roleDescription
        FROM User u
        JOIN u.userRoles ur
        JOIN ur.role r
        WHERE u.deleted = false
        """)
    List<UserListProjection> findAllUserListProjection();
}

    /*JpaRepository giúp: save(),findById(),findAll(),delete(),..
    * JpaSpecificationExecutor cho phép chúng ta thực hiện:
    * Dynamic Query(Truy vấn động)
    +
    Pagination
    +
    Sorting*/
