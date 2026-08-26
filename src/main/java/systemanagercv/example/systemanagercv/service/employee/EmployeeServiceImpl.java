package systemanagercv.example.systemanagercv.service.employee;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import systemanagercv.example.systemanagercv.entity.Employee;
import systemanagercv.example.systemanagercv.repository.EmployeeRepository;

import java.util.List;
@Service
@RequiredArgsConstructor // tự sinh Constructor cho các biến final (Sạch sẽ và chuyên nghiệp nhất)
/* Thay vì Cách 1: dùng tiêm phụ thuộc = @Autowired: cx đc nhưng hơi rối mắt
* Cách 2: Vt constructor để an toàn hơn, nhưng lại viết dài
*  VD:    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;

    // Phải viết đống code này bằng tay:
    public EmployeeService(EmployeeRepository employeeRepository, ModelMapper modelMapper) {
        this.employeeRepository = employeeRepository;
        this.modelMapper = modelMapper;*/

public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    /**
     * 1. XEM DANH SÁCH TOÀN BỘ NHÂN VIÊN
     */
    @Override
    public List<Employee> getAll() {
        return this.employeeRepository.findAll();
    }

    /**
     * 2. LẤY RA 1 EMPLOYEE THEO ID
     * Sử dụng orElseThrow để ném ra lỗi rõ ràng nếu người dùng truyền sai ID bừa bãi
     */
    @Override
    public Employee findById(long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID" + id));
    }

    /**
     * 3. THÊM MỚI NHÂN VIÊN
     */
    @Override
    @Transactional  // Đảm bảo an toàn, lỗi là tự động hoàn nguyên dữ liệu (Rollback)
    //Ví dụ như trong quá trình nhập thông tin mà sập hệ thống thì sẽ k lưu những dữ liệu thiếu vào DB tránh bẩn dữ liệu
    public Employee save(Employee employee) {
        //Cột created_at sẽ tự động sinh ngày giờ nhờ hàm @PrePersist trong Entity
        return this.employeeRepository.save(employee);
    }
    /**
     * 4. CẬP NHẬT (SỬA) THÔNG TIN NHÂN VIÊN
     */
    @Override
    public Employee update(Long id, Employee employee) {
        Employee existingEmployee = this.findById(id);

        existingEmployee.setUser(employee.getUser());
        existingEmployee.setEmployeeCode(employee.getEmployeeCode());
        existingEmployee.setFullName(employee.getFullName());
        existingEmployee.setEmail(employee.getEmail());
        existingEmployee.setPhone(employee.getPhone());
        existingEmployee.setDepartment(employee.getDepartment());
        existingEmployee.setPosition(employee.getPosition());
        existingEmployee.setJobTitle(employee.getJobTitle());
        existingEmployee.setJoinDate(employee.getJoinDate());
        existingEmployee.setStatus(employee.getStatus());
        return employeeRepository.save(existingEmployee);
    }

    /**
     * 5. XÓA NV THEO ID
     */
    @Override
    @Transactional
    public void delete(Long id) {
        //Tìm xem nhân viên có tồn tại không trước khi xóa để tránh lỗi sập DB
        Employee employee = this.findById(id);
        //Tiến hành xóa sạch khỏi MariaDB
        this.employeeRepository.delete(employee);
    }

    @Override
    public boolean existsByEmployeeCode(String employeeCode) {
        return employeeRepository.existsByEmployeeCode(employeeCode);
    }

    @Override
    public boolean existsByUserId(long userId) {
        return employeeRepository.existsByUserId(userId);
    }

    //Lấy tất cả dữ liệu ra để phân trang
    @Override
    public Page<Employee> getAll(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 3);
        return this.employeeRepository.findAll(pageable);
    }

    //Tìm kiếm phân trang
    @Override
    public Page<Employee> searchEmployee(String keyword, Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 3);

        return employeeRepository
                .findByEmployeeCodeContainingIgnoreCaseOrFullNameContainingIgnoreCase(
                        keyword.trim(),
                        keyword.trim(),
                        pageable
                );
    }


}
