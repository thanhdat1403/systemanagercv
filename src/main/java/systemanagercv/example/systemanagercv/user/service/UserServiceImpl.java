package systemanagercv.example.systemanagercv.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import systemanagercv.example.systemanagercv.role.entity.Role;
import systemanagercv.example.systemanagercv.user.dto.request.UserSearchRequest;
import systemanagercv.example.systemanagercv.user.entity.User;
import systemanagercv.example.systemanagercv.role.entity.UserRole;
import systemanagercv.example.systemanagercv.role.entity.UserRoleId;
import systemanagercv.example.systemanagercv.role.repository.RoleRepository;
import systemanagercv.example.systemanagercv.user.repository.UserRepository;
import systemanagercv.example.systemanagercv.user.specification.UserSpecification;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor //tự động viết hộ một hàm khởi tạo (Constructor) chứa tất cả các biến được khai báo với từ khóa final
public class UserServiceImpl implements UserService {

    // Phải có chữ final, Spring Boot sẽ tự hiểu và nạp các linh kiện này vào kho
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    // =====================================================
    // LẤY TẤT CẢ USER
    // =====================================================

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }


    // =====================================================
    // TÌM USER THEO ID
    // =====================================================

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }


    // =====================================================
    // TÌM USER THEO USERNAME
    // =====================================================

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }


    // =====================================================
    // LƯU USER
    // =====================================================

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }


    // =====================================================
    // UPDATE USER
    // =====================================================

    @Override
    public User update(User user) {
        return userRepository.save(user);
    }


    // =====================================================
    // DELETE USER
    // =====================================================

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }


    // =====================================================
    // USER CÓ ROLE EMPLOYEE VÀ CHƯA CÓ EMPLOYEE
    // =====================================================

    @Override
    public List<User> getEmployeeUsers() {

        return userRepository.findUsersAvailableForEmployee(
                "EMPLOYEE"
        );
    }


    // =====================================================
    // USER DÙNG KHI EDIT EMPLOYEE
    // =====================================================

    @Override
    public List<User> getEmployeeUsersForEdit(Long employeeId) {

        return userRepository.findUsersAvailableForEmployeeEdit(
                "EMPLOYEE",
                employeeId
        );
    }

    // =====================================================
    // TẠO USER + ROLE
    // =====================================================

    @Override
    public User createUser(User user, Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Role!"));
        //Mã hóa password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        //User mặc định được kích hoạt
        user.setEnabled(true);
        //Lưu user trước để có user.id
        User savedUser = userRepository.save(user);
        //Tạo UserRole
        UserRole userRole = new UserRole();
        userRole.setUser(savedUser);
        userRole.setRole(role);

        //Vì UserRole sử dụng EmbeddedId
        userRole.setId(
                new UserRoleId(savedUser.getId(), role.getId())
        );

        //Thêm UserRole vào User
        if (savedUser.getUserRoles() == null) {
            savedUser.setUserRoles(new HashSet<>()); //Khi nào cần lưu tập hợp dữ liệu cấm trùng lặp tuyệt đối (như danh sách quyền hạn, danh sách số điện thoại, danh sách email): Dùng new HashSet<>(),HashSet (Set) giúp bảo vệ hệ thống không bao giờ bị lỗi trùng lặp dữ liệu
        }
        savedUser.getUserRoles().add(userRole);
        return userRepository.save(savedUser);
    }

    // =====================================================
    // UPDATE USER + ROLE
    // =====================================================

    @Override
    public User updateUser(User user, Long roleId) {

        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy User!")
                );

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy Role!")
                );

        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        existingUser.setEnabled(user.isEnabled());

        // Nếu nhập password mới
        if (user.getPassword() != null
                && !user.getPassword().trim().isEmpty()) {

            existingUser.setPassword(
                    passwordEncoder.encode(user.getPassword())
            );
        }

        // Xóa Role cũ
        existingUser.getUserRoles().clear();

        // Tạo Role mới
        UserRole userRole = new UserRole();

        userRole.setUser(existingUser);
        userRole.setRole(role);

        userRole.setId(
                new UserRoleId(
                        existingUser.getId(),
                        role.getId()
                )
        );

        existingUser.getUserRoles().add(userRole);

        return userRepository.save(existingUser);
    }

    //Phân trang, tìm kiếm và sắp xếp
    @Override
    public Page<User> search(UserSearchRequest request) {

        Sort sort = Sort.by(
                Sort.Direction.DESC,
                request.getSortBy()
        );

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                sort
        );
        //Đây chính là trả về nơi Dynamic Query + Pagination kết hợp với nhau
        return userRepository.findAll(
                UserSpecification.search(request),
                pageable
        );
    }
}