package systemanagercv.example.systemanagercv.role.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "roles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_roles_name",
                        columnNames = "name"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "name",
            nullable = false,
            length = 50
    )
    private String name;

    @Column(
            name = "description",
            length = 255
    )
    private String description;

    @OneToMany(
            mappedBy = "role",
            fetch = FetchType.LAZY
    )
    private Set<UserRole> userRoles = new HashSet<>();
}