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

    @Column(
            name = "username",
            nullable = false,
            length = 100
    )
    private String username;

    @Column(
            name = "password",
            nullable = false,
            length = 255
    )
    private String password;

    @Column(
            name = "email",
            length = 255
    )
    private String email;

    @Column(
            name = "enabled",
            nullable = false
    )
    private boolean enabled = true;

    @OneToOne(mappedBy = "user")
    private Employee employee;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private Set<UserRole> userRoles = new HashSet<>();
}