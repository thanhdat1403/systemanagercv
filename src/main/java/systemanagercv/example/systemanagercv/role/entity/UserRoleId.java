package systemanagercv.example.systemanagercv.role.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data // NHÃN LOMBOK: Tự sinh toàn bộ hàm Getter, Setter, toString, equals và hashCode ngầm
@NoArgsConstructor // NHÃN LOMBOK: Tự tạo một hàm khởi tạo (Constructor) KHÔNG tham số trống rỗng cho JPA dùng
@AllArgsConstructor // NHÃN LOMBOK: Tự tạo một hàm khởi tạo có ĐẦY ĐỦ tham số (userId, roleId) để bạn tiện truyền dữ liệu
@Embeddable // NHÃN JPA: Báo cho Hibernate biết Class này là "nhân" khóa chính hỗn hợp, dùng để nhúng vào Class Entity trung gian khác
public class UserRoleId implements Serializable { // Serializable giúp Java có thể mã hóa/chuyển đổi cấu trúc khóa này khi truyền dữ liệu ngầm

    @Column(name = "user_id") // Ánh xạ biến này vào cột tên là 'user_id' trong bảng cơ sở dữ liệu MariaDB
    private Long userId;

    @Column(name = "role_id") // Ánh xạ biến này vào cột tên là 'role_id' trong bảng cơ sở dữ liệu MariaDB
    private Long roleId;
}
