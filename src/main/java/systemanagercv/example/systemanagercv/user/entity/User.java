package systemanagercv.example.systemanagercv.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import systemanagercv.example.systemanagercv.common.entity.BaseEntity;
import systemanagercv.example.systemanagercv.employee.entity.Employee;
import systemanagercv.example.systemanagercv.role.entity.UserRole;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_users_username",
                        columnNames = "username"
                ),
                @UniqueConstraint(
                        name = "uk_users_email",
                        columnNames = "email"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity {

    // =====================================================
    // USERNAME
    // =====================================================

    @Column(
            name = "username",
            nullable = false,
            length = 100
    )
    private String username;

    // =====================================================
    // FullName
    // =====================================================
    @Column(
            name = "full_name",
            length = 255
    )
    private String fullName;

    // =====================================================
    // PASSWORD
    // =====================================================

    @Column(
            name = "password",
            nullable = false,
            length = 255
    )
    private String password;


    // =====================================================
    // EMAIL
    // =====================================================

    @Column(
            name = "email",
            length = 255
    )
    private String email;


    // =====================================================
    // ENABLED
    // =====================================================

    @Column(
            name = "enabled",
            nullable = false
    )
    private boolean enabled = true;


    // =====================================================
    // AVATAR CHÍNH THỨC
    // =====================================================

    /**
     * Object key của avatar chính thức hiện tại
     * được lưu trên MinIO.
     *
     * Ví dụ:
     *
     * profile/avatar/official/5/avatar.jpg
     *
     * Database chỉ lưu object key,
     * không lưu file ảnh trực tiếp.
     */
    @Column(
            name = "avatar_object_key",
            length = 500
    )
    private String avatarObjectKey;


    // =====================================================
    // EMPLOYEE
    // =====================================================

    @OneToOne(mappedBy = "user")
    private Employee employee;


    // =====================================================
    // USER ROLES
    // =====================================================

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<UserRole> userRoles = new HashSet<>();
}