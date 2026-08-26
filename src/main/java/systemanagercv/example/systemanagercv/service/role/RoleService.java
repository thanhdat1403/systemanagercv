package systemanagercv.example.systemanagercv.service.role;

import systemanagercv.example.systemanagercv.entity.Role;

import java.util.List;

public interface RoleService {

    List<Role> getAll();

    Role findById(Long id);

    Role findByName(String name);
}
