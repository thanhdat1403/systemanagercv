package systemanagercv.example.systemanagercv.role.service;

import systemanagercv.example.systemanagercv.role.entity.Role;

import java.util.List;

public interface RoleService {

    List<Role> getAll();

    Role findById(Long id);

    Role findByName(String name);
}
