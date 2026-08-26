package systemanagercv.example.systemanagercv.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import systemanagercv.example.systemanagercv.entity.Employee;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    //Kiểm tra mã employee đã tồn tại chưa
    boolean existsByEmployeeCode(String employeeCode);

    //Kiểm tra id user này đã tồn tại chưa
    boolean existsByUserId(Long userId);

    /*(Vế 1) EmployeeCodeContainingIgnoreCase:Tìm theo cột employee_code.
    * Chữ Containing tương đương với lệnh LIKE %giá-trị% (tìm kiếm một đoạn ký tự, không cần gõ chính xác cả từ).
    * Chữ IgnoreCase nghĩa là không phân biệt chữ hoa chữ thường (gõ nv01 hay NV01 đều ra).*/

    /*(Vế 2) FullNameContainingIgnoreCase:Tương tự vế 1 nhưng áp dụng cho cột full_name (tìm kiếm họ tên chứa từ khóa và không phân biệt hoa thường).*/

    List<Employee> findByEmployeeCodeContainingIgnoreCaseOrFullNameContainingIgnoreCase(
            String employeeCode,
            String fullName
    );

    //Tìm kiếm phân trang
    Page<Employee> findByEmployeeCodeContainingIgnoreCaseOrFullNameContainingIgnoreCase(
            String employeeCode,
            String fullName,
            Pageable pageable
    );
}
