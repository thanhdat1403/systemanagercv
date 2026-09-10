package systemanagercv.example.systemanagercv.user.projection;

/**
 * Projection dùng để lấy dữ liệu tối thiểu của User
 * phục vụ dropdown chọn tài khoản khi tạo/chỉnh sửa Employee.
 *
 * Không lấy toàn bộ Entity User.
 */
public interface UserSelectProjection {
    // ID của User
    Long getId();

    //Username của User
    String getUsername();

    //Tên Role của User
    String getRoleName();
}
