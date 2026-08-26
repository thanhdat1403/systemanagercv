package systemanagercv.example.systemanagercv.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import systemanagercv.example.systemanagercv.entity.User;
import systemanagercv.example.systemanagercv.repository.UserRepository;
import systemanagercv.example.systemanagercv.security.CustomUserDetails;
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    // TÁC DỤNG: Tự động tìm kiếm "kho chứa lệnh database" (UserRepository Bean) trong hệ thống
    // và cắm (tiêm) thẳng vào biến này để lập trình viên sử dụng ngay mà không bị lỗi NullPointerException.
    private UserRepository userRepository;

    /**
     * HÀM TÌM KIẾM TÀI KHOẢN KHI ĐĂNG NHẬP
     * Khi người dùng bấm nút "Sign In", Spring Security sẽ tự động ném cái tên đăng nhập vào hàm này.
     */
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));
//        return new CustomUserDetails(user);
//    }
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        System.out.println("========== LOGIN DEBUG ==========");

        System.out.println("Username nhận được: " + username);

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException(
                    "Không tìm thấy user: " + username
            );
        }

        System.out.println("Tìm thấy user ID: " + user.getId());

        System.out.println("Username DB: " + user.getUsername());

        System.out.println("Enabled: " + user.isEnabled());

        System.out.println("Password DB: " + user.getPassword());

        System.out.println("Số role: " + user.getUserRoles().size());

        user.getUserRoles().forEach(ur -> {
            System.out.println(
                    "Role = " + ur.getRole().getName()
            );
        });

        System.out.println("===============================");

        return new CustomUserDetails(user);
    }

    /*admin
     ↓
    CustomUserDetailsService
     ↓
    UserRepository
     ↓
    users
     ↓
    user_roles
     ↓
    roles
     ↓
    CustomUserDetails
     ↓
    Spring Security*/

}
