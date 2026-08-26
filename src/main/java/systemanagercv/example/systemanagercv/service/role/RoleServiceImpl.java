package systemanagercv.example.systemanagercv.service.role;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import systemanagercv.example.systemanagercv.entity.Role;
import systemanagercv.example.systemanagercv.repository.RoleRepository;

import java.util.List;
@Service
@RequiredArgsConstructor // Tự động sinh hàm khởi tạo cho các biến final ở dưới
public class RoleServiceImpl implements RoleService {
    // Phải có chữ final, Spring Boot sẽ tự hiểu và nạp các linh kiện này vào kho
    private final RoleRepository roleRepository;

    @Override
    public List<Role> getAll() {
        return roleRepository.findAll();
    }

    @Override
    public Role findById(Long id) {
        return roleRepository.findById(id).orElse(null);
    }

    @Override
    public Role findByName(String name) {
        return roleRepository.findByName(name).orElse(null);
    }
}
