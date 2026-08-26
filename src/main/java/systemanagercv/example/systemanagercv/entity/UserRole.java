package systemanagercv.example.systemanagercv.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    //khi một bảng mà khóa chính được gộp từ 2 cột trở lên (thường gặp nhất ở các bảng trung gian của mối quan hệ Nhiều - Nhiều).
    @EmbeddedId // 💡 Nhãn báo cho Spring biết: Class này dùng để nhúng làm khóa chính
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