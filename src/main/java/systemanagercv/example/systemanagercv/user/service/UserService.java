package systemanagercv.example.systemanagercv.user.service;

import systemanagercv.example.systemanagercv.user.entity.User;

import java.util.List;

public interface UserService {

    List<User> getAll();

    User findById(Long id);

    User findByUsername(String username);

    User save(User user);

    User update(User user);

    void delete(Long id);

    // User EMPLOYEE chưa được liên kết Employee
    List<User> getEmployeeUsers();

    // User EMPLOYEE chưa được liên kết
    // + User hiện tại của Employee đang sửa
    List<User> getEmployeeUsersForEdit(Long employeeId);

    //Để Service xử lý nghiệp vụ vì để cho Admin khi tạo User, phải đồng thời tạo UserRole
    User createUser(User user, Long roleId);
    User updateUser(User user, Long roleId);
}