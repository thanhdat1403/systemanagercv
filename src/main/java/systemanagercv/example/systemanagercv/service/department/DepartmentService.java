package systemanagercv.example.systemanagercv.service.department;

import org.springframework.data.domain.Page;
import systemanagercv.example.systemanagercv.entity.Departments;

import java.util.List;

public interface DepartmentService {
    List<Departments> getAll(); //Xem danh sách phòng ban
    Departments findById(Long id); // Lấy ra 1 phòng ban theo ID
    Departments create(Departments departments); //Thêm mới phòng ban
    Departments update(Long id, Departments departments); // Cập nhật phòng ban
    void delete(Long id); //Xóa phòng ban


    //Phân trang
    Page<Departments> getAll(Integer pageNo);
    //Kết hợp tìm kiếm + phân trang
    Page<Departments> searchDepartment(String keyword, Integer pageNo);
}
