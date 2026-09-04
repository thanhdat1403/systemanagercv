package systemanagercv.example.systemanagercv.security;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import systemanagercv.example.systemanagercv.user.entity.User;
import systemanagercv.example.systemanagercv.user.repository.UserRepository;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        System.out.println("========== LOGIN DEBUG ==========");

        System.out.println(
                "Username nhận được: " + username
        );

        User user =
                userRepository.findByUsername(username);

        if (user == null) {

            throw new UsernameNotFoundException(
                    "Không tìm thấy user: " + username
            );
        }

        System.out.println(
                "Tìm thấy user ID: " + user.getId()
        );

        System.out.println(
                "Username DB: " + user.getUsername()
        );

        System.out.println(
                "Enabled: " + user.isEnabled()
        );

        System.out.println(
                "Password DB: " + user.getPassword()
        );

        System.out.println(
                "Số role: " + user.getUserRoles().size()
        );

        user.getUserRoles().forEach(userRole -> {

            System.out.println(
                    "Role = "
                            + userRole.getRole().getName()
            );
        });

        System.out.println(
                "================================"
        );

        return new CustomUserDetails(user);
    }
}
/*Lớp này phục vụ: Spring Security
Login
Load username/password/role*/