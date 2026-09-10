package systemanagercv.example.systemanagercv.department.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import systemanagercv.example.systemanagercv.common.exception.BusinessException;
import systemanagercv.example.systemanagercv.department.dto.request.DepartmentCreateRequest;
import systemanagercv.example.systemanagercv.department.dto.request.DepartmentSearchRequest;
import systemanagercv.example.systemanagercv.department.dto.request.DepartmentUpdateRequest;
import systemanagercv.example.systemanagercv.department.dto.response.DepartmentDetailResponse;
import systemanagercv.example.systemanagercv.department.dto.response.DepartmentResponse;
import systemanagercv.example.systemanagercv.department.entity.Departments;
import systemanagercv.example.systemanagercv.department.enums.DepartmentStatus;
import systemanagercv.example.systemanagercv.department.mapper.DepartmentMapper;
import systemanagercv.example.systemanagercv.department.repository.DepartmentRepository;
import systemanagercv.example.systemanagercv.department.specification.DepartmentSpecification;

import java.util.List;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    private final DepartmentMapper departmentMapper;

    /**
     * ============================================================
     * 1. TÌM KIẾM NÂNG CAO + PHÂN TRANG + SẮP XẾP PHÒNG BAN
     * ============================================================
     */
    @Override
    public Page<DepartmentResponse> search(
            DepartmentSearchRequest request
    ) {
        //1.1. Xác định chiều sắp xếp: Nếu client truyền lên chữ "DESC" (không phân biệt hoa/thường) thì chọn Giảm dần, ngược lại chọn Tăng dần (ASC)
        Sort.Direction direction =
                "DESC".equalsIgnoreCase(request.getSortDirection())
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        // 1.2. Nhờ hàm helper phía dưới kiểm tra xem tên cột muốn sort có hợp lệ không (Chống Hacker)
        String sortBy = validateSortField(request.getSortBy());

        // 1.3. Khởi tạo đối tượng phân trang Pageable tích hợp quy tắc sắp xếp vừa cấu hình
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(direction, sortBy)
        );

        // 1.4. ĐÂY CHÍNH LÀ NƠI XẾP HÌNH LEGO SPECIFICATION:
        // Ghép nối 3 hàm lọc nhỏ từ file DepartmentSpecification lại thành 1 câu lệnh logic hoàn chỉnh
        // Tương đương SQL: WHERE deleted = false AND (code LIKE ... OR name LIKE ...) AND status = ...
        Specification<Departments> specification =
                Specification
                        .where(DepartmentSpecification.notDeleted()) // Điều kiện bắt buộc: Chưa bị xóa mềm
                        .and(
                                DepartmentSpecification.keyword(
                                        request.getKeyword() // Điều kiện kèm theo: Khớp từ khóa tìm kiếm (nếu có)
                                )
                        )
                        .and(
                                DepartmentSpecification.hasStatus(
                                        request.getStatus() // Điều kiện kèm theo: Đúng trạng thái phòng ban (nếu có)
                                )
                        );

        // 1.5. Đẩy câu lệnh xuống Database lấy lên một trang dữ liệu thô và lặp đổi ruột thành DepartmentResponse sạch đẹp
        return departmentRepository
                .findAll(specification, pageable)
                .map(departmentMapper::toResponse);
    }

    /**
     * ============================================================
     * 2. LẤY THÔNG TIN CHI TIẾT PHÒNG BAN ĐỂ HIỂN THỊ RA API
     * ============================================================
     */
    @Override
    public DepartmentDetailResponse findById(Long id) {

        // Vào DB tìm kiếm phòng ban theo ID, dùng bộ lọc filter loại bỏ ngay nếu phòng ban đó đã bị đánh dấu xóa mềm
        // Nếu không thấy ID hợp lệ -> Lập tức dừng code lại và rút thẻ đỏ ném lỗi nghiệp vụ
        Departments departments =
                departmentRepository.findById(id)
                        .filter(dept ->
                                !dept.isDeleted()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "error.departments.not.found"
                                )
                        );
        // Chuyển đổi dữ liệu sang gói DTO chi tiết để trả về kết quả thành công
        return departmentMapper.toDetailResponse(departments);
    }

    /**
     * ============================================================
     * 3. THÊM MỚI MỘT PHÒNG BAN VÀO HỆ THỐNG
     * ============================================================
     */
    @Override
    @Transactional // Ghi đè nhãn lớn: Cho phép thực hiện Thêm/Sửa/Xóa dữ liệu và bật bảo hiểm tự động Rollback nếu lỗi
    public DepartmentResponse create(
            DepartmentCreateRequest request
    ) {
        //Cắt bỏ khoảng trắng thừa của Mã phòng ban đầu vào
        String code = request.getCode().trim();

        // Nhờ Repository quét xuống DB xem có phòng ban nào chưa xóa đang sử dụng cái 'code' này rồi chưa
        // Nếu đã tồn tại -> chặn đứng lại và báo lỗi "Mã phòng ban đã tồn tại"
        if (departmentRepository
                .existsByCodeAndDeletedFalse(code)) {
            throw new BusinessException("error.departments.code.exists");
        }

        // Nhờ Mapper biến đổi dữ liệu người dùng nhập từ Form (Request DTO)
        Departments departments =
                departmentMapper.toEntity(request);

        // Gán lại mã code sạch đã được cắt khoảng trắng
        departments.setCode(code);

        // Chính thức lưu dữ liệu vào Database
        Departments saved =
                departmentRepository.save(departments);

        // Chuyển đổi đối tượng vừa lưu xong thành dữ liệu sạch để phản hồi về cho màn hình
        return departmentMapper.toResponse(saved);
    }

    /**
     * ============================================================
     * 4. CẬP NHẬT (SỬA) THÔNG TIN PHÒNG BAN ĐANG CÓ SẴN
     * ============================================================
     */
    @Override
    @Transactional // Bật bảo hiểm ghi/sửa dữ liệu và tự động hủy lệnh nếu xảy ra lỗi giữa chừng
    public DepartmentResponse update(
            Long id,
            DepartmentUpdateRequest request
    ) {
        // 4.1. Tìm phòng ban cũ cần sửa đổi trong DB theo ID truyền vào và đảm bảo nó chưa bị xóa
        Departments departments =
                departmentRepository.findById(id)
                        .filter(existing ->
                                !existing.isDeleted()
                        ).orElseThrow(() ->
                                new BusinessException("error.departments.not.found")
                        );
        String code = request.getCode().trim();

        // 4.2. Kiểm tra trùng mã: Có phòng ban của NGƯỜI KHÁC (IdNot) đang sở hữu mã 'code' mới này không?
        // Kỹ thuật này giúp loại trừ chính nó ra (Nếu giữ nguyên mã cũ chỉ sửa tên thì hàm sẽ bỏ qua không báo lỗi trùng)
        if (departmentRepository
                .existsByCodeAndIdNotAndDeletedFalse(
                        code,
                        id
                )) {
            throw new BusinessException("error.departments.code.exists");

        }

        // 4.3. Nhờ mapper tự động lấy các thông tin mới đè lên đối tượng dữ liệu cũ
        departmentMapper.updateEntity(
                request,
                departments
        );

        departments.setCode(code); // Gán mã sạch

        // 4.4. Lưu lại thông tin mới cập nhật xuống Database
        Departments updated =
                departmentRepository.save(departments);
        return departmentMapper.toResponse(updated);
    }

    /**
     * ============================================================
     * 5. XÓA MỀM PHÒNG BAN (SOFT DELETE)
     * ============================================================
     */
    @Override
    @Transactional // Bật bảo hiểm ghi/sửa dữ liệu xuống DB
    public void delete(Long id) {

        // Tìm kiếm phòng ban cần xóa theo ID và chắc chắn nó chưa bị ẩn
        Departments departments =
                departmentRepository.findById(id)
                        .filter(existing ->
                                !existing.isDeleted()
                        ).orElseThrow(() ->
                                new BusinessException("error.departments.not.found")
                        );
        /*
         * Thực hiện kỹ thuật Xóa mềm (Soft delete):
         * Tuyệt đối không xóa hẳn hàng trong DB. Chỉ cần bật cờ trạng thái 'deleted' từ false thành true.
         * Bộ lọc Specification ở Hàm số 1 sẽ tự động ẩn phòng ban này đi ở tất cả các màn hình tìm kiếm.
         */
        departments.setDeleted(true);

        //Lưu lại xuống DB
        departmentRepository.save(departments);
    }

    /**
     * Tìm và trả về trực tiếp đối tượng thực thể gốc (Entity) của Phòng ban theo ID.
     * Chức năng: Chuyên phục vụ để gọi nội bộ giữa các Service với nhau (Ví dụ: Dùng trong EmployeeService).
     */
    @Override
    public Departments findEntityById(Long id) {
        // 1. Vào Database tìm phòng ban theo ID truyền vào
        return departmentRepository.findById(id)
        // 2. Chốt chặn filter: Kiểm tra xem phòng ban này đã bị đánh dấu xóa mềm (isDeleted = true) chưa.
        // Nếu ĐÃ XÓA MỀM -> Bộ lọc trả về rỗng, lập tức kích hoạt lệnh orElseThrow phía dưới.
                .filter(departments ->
                        !departments.isDeleted()
                )
                // 3. Nếu tìm thấy và chưa bị xóa thì đi tiếp, ngược lại lập tức rút thẻ đỏ ném lỗi nghiệp vụ
                .orElseThrow(() ->
                        new BusinessException(
                                "error.department.notFound"
                        )
                );
    }

    @Override
    public List<DepartmentResponse> getActiveDepartments() {

        return departmentRepository.findAll(
                        Specification
                                .where(DepartmentSpecification.notDeleted())
                                .and(DepartmentSpecification.hasStatus(DepartmentStatus.ACTIVE))
                )
                .stream()
                .map(departmentMapper::toResponse)
                .toList();
    }

    /**
     * Bộ lọc an toàn (Whitelist): Kiểm tra và chuẩn hóa tên trường dữ liệu dùng để Sắp xếp (Sort).
     * Tác dụng: Ngăn chặn tuyệt đối việc client truyền tên cột bậy bạ lên URL làm lỗi câu lệnh SQL dưới Database.
     */
    private String validateSortField(String sortBy) {
        // Sử dụng cú pháp Switch Expression đời mới của Java
        return switch (sortBy){

            // Nếu từ khóa client gửi lên khớp chính xác với danh sách an toàn dưới đây -> Trả về đúng từ khóa đó
            case "id" -> "id";

            case "code" -> "code";

            case "name" -> "name";

            case "status" -> "status";

            case "createdDate" -> "createdDate";

            case "updatedDate" -> "updatedDate";

            // Nếu người dùng không truyền (null) hoặc cố tình gõ linh tinh trường không tồn tại
            // -> Trừng phạt bằng cách ép quay về sắp xếp theo cột mặc định an toàn là "id"
            default -> "id";
        };
    }
}
