package systemanagercv.example.systemanagercv.department.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import systemanagercv.example.systemanagercv.department.dto.request.DepartmentRequest;
import systemanagercv.example.systemanagercv.department.dto.response.DepartmentResponse;
import systemanagercv.example.systemanagercv.department.entity.Departments;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {
//    Mapper có nv chuyển đổi: DepartmentRequest → Departments
//    và Departments → DepartmentResponse

    /**
     * Chuyển dữ liệu từ Request DTO → Entity
     *
     * Dùng khi:
     * - Thêm phòng ban
     * - Cập nhật phòng ban
     */
    Departments toEntity(DepartmentRequest request);

    /**
     * DepartmentRequest → Departments đã tồn tại
     *
     * Dùng khi cập nhật phòng ban.
     */
    void updateEntity(
            DepartmentRequest request,
            @MappingTarget Departments departments
    );

    /**
     * Chuyển dữ liệu từ Entity → Response DTO
     *
     * Dùng khi:
     * - Trả danh sách phòng ban
     * - Xem chi tiết phòng ban
     */
    DepartmentResponse toResponse(Departments departments);
}
/*Hiểu về Mapper (Nỗi đau nếu k có Mapper)
NẾU KHÔNG CÓ MAPPER: Nếu không tạo file Mapper, bạn sẽ phải tự tay viết code get/set thủ công cho từng trường dữ liệu ở tầng Service.
Hãy tưởng tượng bảng Nhân viên của bạn có 20 trường dữ liệu, mỗi lần Thêm, Sửa hoặc Xem, bạn phải gõ đi gõ lại 20 dòng code nhàm chán như thế này:
// ❌ Cực hình nếu không có Mapper: Code Service bị dài dòng, rác và loãng logic
Employee employee = new Employee();
employee.setFullName(request.getFullName());
employee.setEmail(request.getEmail());
employee.setPhone(request.getPhone());
employee.setJobTitle(request.getJobTitle());
// ... Gõ mỏi tay cho đến hết 20 trường

 */

/*LUỒNG HOẠT ĐỘNG:
*                📥 CHIỀU GỬI DỮ LIỆU VÀO SYSTEM
[Form HTML] ➔ [Request DTO] ────( MAPPER chuyển đổi )────> [Entity] ➔ [Lưu Database]
                                                                        │
               📤 CHIỀU TRẢ DỮ LIỆU RA MÀN HÌNH                          │
[Giao diện] ◄─ [Response DTO] ◄───( MAPPER làm phẳng )──── [Entity] ◄───┘
*/
