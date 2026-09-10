package systemanagercv.example.systemanagercv.department.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import systemanagercv.example.systemanagercv.department.entity.Departments;
/*
* JpaRepository: Cung cấp sẵn các hàm cơ bản (save, findById, findAll, delete...).
* JpaSpecificationExecutor: Cho phép truyền bộ lọc động (Specification) để tìm kiếm nâng cao + phân trang.
* */
public interface DepartmentRepository
        extends JpaRepository<Departments, Long>,
        JpaSpecificationExecutor<Departments> {

    /**
     * Kiểm tra mã phòng ban đã tồn tại trên hệ thống chưa (Dùng khi THÊM MỚI).
     *
     * Spring tự dịch thành SQL:
     * SELECT COUNT(*) FROM departments WHERE code = :code AND deleted = false;
     *
     * @return true nếu đã có phòng ban sở hữu mã này, false nếu mã này còn trống.
     */
    boolean existsByCodeAndDeletedFalse(String code);

    /**
     * Kiểm tra mã phòng ban mới nhập có bị trùng với phòng ban của NGƯỜI KHÁC hay không (Dùng khi CẬP NHẬT).
     *
     * Spring tự dịch thành SQL:
     * SELECT COUNT(*) FROM departments WHERE code = :code AND id <> :id AND deleted = false;
     *
     * @param code Mã phòng ban mới muốn thay đổi
     * @param id ID của phòng ban hiện tại đang sửa (để loại trừ chính nó ra, không so sánh trùng với chính mình)
     * @return true nếu mã mới bị trùng với một phòng ban khác, false nếu hợp lệ.
     */
    boolean existsByCodeAndIdNotAndDeletedFalse(
            String code,
            Long id
    );

}
