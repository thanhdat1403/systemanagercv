package systemanagercv.example.systemanagercv.security;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import systemanagercv.example.systemanagercv.user.entity.User;
import systemanagercv.example.systemanagercv.role.entity.UserRole;

import java.util.Collection;
import java.util.stream.Collectors;

// Implements UserDetails: Đóng gói tài khoản User của bạn thành cấu trúc chuẩn mà Spring Security có thể đọc hiểu.
public class CustomUserDetails implements UserDetails {

    // Khai báo thực thể User gốc lấy lên từ Database MariaDB
    private final User user;

    // Hàm khởi tạo: Nhận vào một User thô từ DB và nạp vào lớp bọc bảo mật này
    public CustomUserDetails(User user) {
        this.user = user;
    }

    /**
     * HÀM QUAN TRỌNG NHẤT: Bốc quyền hạn từ Database dịch sang ngôn ngữ Spring Security
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getUserRoles() // 1. Lấy ra danh sách các "thẻ gán quyền trung gian" của người dùng
                .stream()          // 2. Bật băng chuyền dữ liệu tự động duyệt qua từng thẻ
                .map(UserRole::getRole) // 3. Đi sâu vào từng thẻ để nhặt lấy thực thể Role (Quyền) thật sự

                // 4. BIẾN ĐỔI: Đọc tên quyền (Ví dụ: "ADMIN") và tự động nối thêm chữ "ROLE_" ở trước thành "ROLE_ADMIN"
                // Cách viết "ROLE_" + role.getName() này giúp bạn không cần gõ chữ ROLE_ dưới database HeidiSQL nữa!
                .map(role -> new SimpleGrantedAuthority(
                        "ROLE_" + role.getName()
                ))
                .collect(Collectors.toSet()); // 5. Gom tất cả các quyền đã dịch thành một tập hợp (Set) trả về cho Security
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // Trả về mật khẩu đã mã hóa của người dùng này
    }

    @Override
    public String getUsername() {
        return user.getUsername(); // Trả về tên đăng nhập (username)
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Báo cho Security biết: Tài khoản này không bao giờ bị hết hạn
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Báo cho Security biết: Tài khoản này đang mở, không bị khóa
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Báo cho Security biết: Thông tin đăng nhập/mật khẩu không bị hết hạn
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled(); // Trả về trạng thái kích hoạt tài khoản thật từ database (true là hoạt động, false là bị tắt)
    }

    // Hàm bổ sung: Giúp bạn sau này ở tầng Controller có thể lấy nhanh thông tin User gốc ra xài (ví dụ lấy fullName hiển thị lên màn hình)
    public User getUser() {
        return user;
    }
}

