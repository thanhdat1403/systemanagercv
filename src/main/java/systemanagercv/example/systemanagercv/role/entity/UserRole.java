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
                        name = "uk_user_roles_user_role",
                        columnNames = {"user_id", "role_id"}
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

    /*fetch = FetchType.LAZY (Tải trì hoãn):Chỉ khi nào thực sự cần dùng đến dữ liệu của bảng liên kết thì mới truy vấn database để lấy về, còn không thì bỏ qua*/
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId") //code cấu hình liên kết khóa ngoại trong Spring Data JPA
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