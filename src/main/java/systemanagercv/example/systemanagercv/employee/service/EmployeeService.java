package systemanagercv.example.systemanagercv.employee.service;

import org.springframework.data.domain.Page;
import systemanagercv.example.systemanagercv.employee.entity.Employee;

import java.util.List;

public interface EmployeeService {
    List<Employee> getAll();

    Employee findById(long id);
    Employee save(Employee employee);
    Employee update(Long id, Employee employee);
    void delete(Long id);

    boolean existsByEmployeeCode(String employeeCode);
    boolean existsByUserId(long userId);

    //Phân trang
    Page<Employee> getAll(Integer pageNo);
    //Kết hợp Tìm kiếm + phân trang
    Page<Employee> searchEmployee(String keyword, Integer pageNo);
}
