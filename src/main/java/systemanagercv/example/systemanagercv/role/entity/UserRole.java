package systemanagercv.example.systemanagercv.role.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import systemanagercv.example.systemanagercv.user.entity.User;

@Entity
@Table(
        name = "user_roles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_roles_user",
                        columnNames = "user_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class UserRole {

    @EmbeddedId
    private UserRoleId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(
            name = "role_id",
            nullable = false
    )
    private Role role;

    public UserRole(User user, Role role) {
        this.user = user;
        this.role = role;

        this.id = new UserRoleId(
                user.getId(),
                role.getId()
        );
    }
}