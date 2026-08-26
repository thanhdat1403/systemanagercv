package systemanagercv.example.systemanagercv.department.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import systemanagercv.example.systemanagercv.department.entity.Departments;
import systemanagercv.example.systemanagercv.department.repository.DepartmentRepository;

import java.util.List;
@Service
public class DepartmentServiceImpl implements DepartmentService {
    @Autowired
    private DepartmentRepository departmentRepository;

    /**
     * 1. XEM DANH SÁCH TOÀN BỘ PHÒNG BAN
     */
    @Override
    public List<Departments> getAll() {
        return this.departmentRepository.findAll();
    }

    /**
     * 2. LẤY RA 1 PHÒNG BAN THEO ID
     * Sử dụng orElseThrow để ném ra lỗi rõ ràng nếu người dùng truyền sai ID bừa bãi
     */
    @Override
    public Departments findById(Long id) {
        return this.departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban với ID" + id));
    }

    /**
     * 3. THÊM MỚI PHÒNG BAN
     */
    @Override
    @Transactional  // Đảm bảo an toàn, lỗi là tự động hoàn nguyên dữ liệu (Rollback)
    //Ví dụ như trong quá trình nhập thông tin mà sập hệ thống thì sẽ k lưu những dữ liệu thiếu vào DB tránh bẩn dữ liệu
    public Departments create(Departments departments) {
        //Cột created_at sẽ tự động sinh ngày giờ nhờ hàm @PrePersist trong Entity
        return this.departmentRepository.save(departments);
    }

    /**
     * 4. CẬP NHẬT (SỬA) THÔNG TIN PHÒNG BAN
     */
    @Override
    @Transactional
    public Departments update(Long id, Departments departments) {
        //Bước A: Tìm xem phòng ban cũ có tồn tại trong DB k, k có tự động ném lỗi
        Departments existingDept = this.findById(id);

        //Bước B: Thay thế dữ liệu cũ bằng dữ liệu mới gửi nên từ giao diện
        existingDept.setCode(departments.getCode());
        existingDept.setName(departments.getName());
        existingDept.setDescription(departments.getDescription());
        existingDept.setStatus(departments.getStatus());
        return this.departmentRepository.save(existingDept);
    }

    /**
     * 5. XÓA PHÒNG BAN THEO ID
     */
    @Override
    @Transactional
    public void delete(Long id) {
        //Tìm xem phòng ban có tồn tại không trước khi xóa để tránh lỗi sập DB
        Departments departments = this.findById(id);

        //Tiến hành xóa sạch khỏi MariaDB
        this.departmentRepository.delete(departments);
    }

    //Tìm kiếm + phân trang

    @Override
    public Page<Departments> getAll(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 2);// Phân trang lấy ra 2 danh mục và có thể thay đổi tùy theo ý muốn
        return this.departmentRepository.findAll(pageable);
    }

    @Override
    public Page<Departments> searchDepartment(String keyword, Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo - 1, 2);
        return departmentRepository.searchDepartment(keyword, pageable);
    }
}
