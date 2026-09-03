package systemanagercv.example.systemanagercv.user.projection;

public interface UserListProjection {

    Long getId();
    String getUsername();
    String getEmail();
    Boolean getEnabled();
    Long getRoleId();
    String getRoleName();
    String getRoleDescription();
}
/*Projection này để làm gì :
* Vì trong Entity User có rất nhiều thứ: id, username, password, email, enable, employee, userRoles, createdDate,createdBy,updatedBy,isDelete
* Nhưng màn hình danh sách User chỉ cần: id, username, email,enabled, roleId roleName, roleDescription
* Thay vì:
* Database
   ↓
Lấy toàn bộ User Entity
   ↓
Lấy UserRole
   ↓
Lấy Role
*
* Thì ta hướng tới:
* Database
   ↓
Chỉ lấy những field cần thiết
   ↓
UserListProjection (Đây chính là quy định 31)*/

/*Có một điểm cần phân biệt:

UserRepository: chỉ truy cập DB.
UserSpecification: xây điều kiện tìm kiếm động.
UserSearchRequest: nhận toàn bộ điều kiện tìm kiếm.
UserListProjection: chỉ lấy các cột cần cho danh sách.
UserMapper: chuyển Projection → Response DTO.
UserService: điều phối các thành phần trên.*/