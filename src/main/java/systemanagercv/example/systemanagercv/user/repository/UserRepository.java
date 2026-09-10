package systemanagercv.example.systemanagercv.user.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import systemanagercv.example.systemanagercv.user.entity.User;
import systemanagercv.example.systemanagercv.user.projection.UserSelectProjection;

import java.util.List;
import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    /**
     * ============================================================
     * USER LOGIN
     * ============================================================
     *
     * Tìm User đang hoạt động theo username.
     *
     * Load luôn:
     * User -> UserRoles -> Role
     *
     * để Spring Security có thể lấy Role khi đăng nhập.
     */

    @EntityGraph(attributePaths = {
            "userRoles",
            "userRoles.role"
    })
    User findByUsernameAndDeletedFalse(String username);


    // =====================================================
    // KIỂM TRA USERNAME / EMAIL
    // =====================================================

    boolean existsByUsernameAndDeletedFalse(String username);

    boolean existsByEmailAndDeletedFalse(String email);



    boolean existsByUsernameAndIdNotAndDeletedFalse(
            String username,
            Long id
    );

    boolean existsByEmailAndIdNotAndDeletedFalse(
            String email,
            Long id
    );

    // ============================================================
    // USER - CHECK ADMIN
    // ============================================================

    /**
     * Kiểm tra đã tồn tại tài khoản ADMIN đang hoạt động hay chưa.
     *
     * Điều kiện:
     * - User chưa bị soft delete
     * - User có Role ADMIN
     *
     * Dùng để đảm bảo hệ thống chỉ có duy nhất 1 ADMIN
     * đang hoạt động.
     */
    boolean existsByUserRoles_Role_NameAndDeletedFalse(String roleName);


    /**
     * ============================================================
     * USER SELECT - CREATE EMPLOYEE
     * ============================================================
     *
     * Lấy danh sách tài khoản có thể gán cho Employee khi TẠO mới.
     *
     * Điều kiện:
     *
     * 1. User chưa bị soft-delete.
     * 2. User đang enabled.
     * 3. User phải có Role phù hợp:
     *      - HR
     *      - TECH_LEAD
     *      - EMPLOYEE
     *
     * 4. User chưa được liên kết với Employee nào.
     *
     * 5. Không cho ADMIN xuất hiện trong dropdown.
     *
     * Chỉ SELECT 3 field cần thiết:
     *      id
     *      username
     *      roleName
     *
     * để map sang UserSelectProjection.
     */
    @Query("""
        SELECT DISTINCT
            u.id AS id,
            u.username AS username,
            r.name AS roleName
        FROM User u
        JOIN u.userRoles ur
        JOIN ur.role r
        LEFT JOIN u.employee e
        WHERE u.deleted = false
          AND u.enabled = true
          AND r.name <> :adminRole
          AND e.id IS NULL
        ORDER BY u.username ASC
        """)
    List<UserSelectProjection> findUsersAvailableForEmployee(
            @Param("adminRole") String adminRole
    );

    /**
     * ============================================================
     * USER SELECT - EDIT EMPLOYEE
     * ============================================================
     *
     * Lấy danh sách tài khoản có thể gán cho Employee khi CHỈNH SỬA.
     *
     * Khác với method phía trên:
     *
     * User hiện đang được Employee này sử dụng
     * vẫn phải xuất hiện trong dropdown.
     *
     * Ví dụ:
     *
     * Employee A -> tech01
     *
     * Khi sửa Employee A:
     *
     * tech01 phải vẫn xuất hiện.
     *
     * Nhưng tech01 sẽ không xuất hiện khi sửa Employee B.
     *
     * Điều kiện:
     *
     * 1. User chưa bị soft-delete.
     * 2. User đang enabled.
     * 3. Không phải ADMIN.
     * 4. User chưa được gán Employee khác.
     * 5. Hoặc User chính là tài khoản hiện tại của Employee đang sửa.
     */
    @Query("""
        SELECT DISTINCT
            u.id AS id,
            u.username AS username,
            r.name AS roleName
        FROM User u
        JOIN u.userRoles ur
        JOIN ur.role r
        LEFT JOIN u.employee e
        WHERE u.deleted = false
          AND u.enabled = true
          AND r.name <> :adminRole
          AND (
                e.id IS NULL
                OR e.id = :employeeId
              )
        ORDER BY u.username ASC
        """)
    List<UserSelectProjection> findUsersAvailableForEmployeeEdit(
            @Param("adminRole") String adminRole,
            @Param("employeeId") Long employeeId
    );



    /**
     * ============================================================
     * USER DETAIL
     * ============================================================
     *
     * Lấy đầy đủ thông tin User phục vụ trang Detail/Edit.
     *
     * Load:
     * - Employee
     * - UserRole
     * - Role
     */
    @Query("""
        SELECT DISTINCT u
        FROM User u
        LEFT JOIN FETCH u.employee e
        LEFT JOIN FETCH u.userRoles ur
        LEFT JOIN FETCH ur.role r
        WHERE u.id = :id
          AND u.deleted = false
        """)
    Optional<User> findUserDetailById(
            @Param("id") Long id
    );
}