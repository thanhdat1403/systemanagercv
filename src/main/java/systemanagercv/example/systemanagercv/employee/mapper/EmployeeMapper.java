package systemanagercv.example.systemanagercv.employee.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import systemanagercv.example.systemanagercv.employee.dto.response.EmployeeResponse;
import systemanagercv.example.systemanagercv.employee.entity.Employee;

/*Giải thích về MapStruct:
* MapStruct là một thư viện (plugin) cực kỳ thông minh trong Java,
*  có tác dụng tự động viết code chuyển đổi dữ liệu (Mapping) qua lại giữa các đối tượng với nhau
*  — điển hình nhất là chuyển từ Entity sang DTO (Response) và từ DTO (Request) sang Entity
* Nếu k có MapStruct thì sẽ phải code tay rất vất vả*/
@Mapper(componentModel = "spring") // Nhãn này giúp MapStruct bắt tay làm vc với Spring Boot. Nếu thiếu không thể dùng cơ chế @Autowired hay Constructor để gọi nó ra sử dụng được.
public interface EmployeeMapper {

    @Mapping(target = "userId", source = "user.id") //Ý nghĩa: Hãy đi vào đối tượng con user(tức là: private User user trong entity Employee) bên trong Employee->Nhặt lấy id của tài khoản đó->Rồi nạp vào userId ở file Response
    @Mapping(target = "username", source = "user.username")

    @Mapping(target = "departmentId", source = "department.id") //Ý nghĩa: Hãy đi vo đối tượng con department(tức: private Department department trong entity Employee)
    @Mapping(target = "departmentCode", source = "department.code")
    @Mapping(target = "departmentName", source = "department.name")

    //Tuy nhiên positionDescription và statusDescription ta đã Enum thì sẽ không thể tự Map được theo cách này
    //Thì sẽ xử lý chúng ở Mapper bằng default method hoặc expression
    @Mapping(
            target = "positionDescription",
            expression = "java(employee.getPosition() != null ? employee.getPosition().getDescription() : null)"
    )
    @Mapping(
            target = "statusDescription",
            expression = "java(employee.getStatus() != null ? employee.getStatus().getDescription() : null)"
    )
    EmployeeResponse toResponse(Employee employee);

    //Luồng xử lý
    /*[BƯỚC 1: REPOSITORY QUÉT DATABASE]
    Mã Java chạy xuống MariaDB lấy thực thể Employee lên. Lúc này thực thể cấu trúc rất cồng kềnh:
    Employee (id=1, employeeCode="NV001", fullName="Nguyễn Văn A")
       ├──> user (id=99, username="anv", password="...")
       └──> department (id=2, code="IT", name="Phòng Công nghệ thông tin")

                                ↓
    [BƯỚC 2: MAPSTRUCTVÀO CUỘC TRUNG CHUYỂN]
    Tầng Service gọi lệnh: EmployeeResponse dto = employeeMapper.toResponse(employee);
    File Mapper sẽ kích hoạt các thẻ @Mapping để bóc tách dữ liệu thô:
      - Tự động copy: fullName ➔ fullName
      - Đi sâu bới link: user.id ➔ userId (Lấy ra số 99)
      - Đi sâu bới link: department.name ➔ departmentName (Lấy ra chữ "Phòng Công nghệ thông tin")

                                ↓
    [BƯỚC 3: ĐÓNG GÓI DỮ LIỆU PHẲNG]
    Kết quả cho ra đời một đối tượng EmployeeResponse phẳng lì, tinh gọn, sạch sẽ, an toàn:
    EmployeeResponse (id=1, username="anv", fullName="Nguyễn Văn A", departmentName="Phòng Công nghệ thông tin")
    *(Mật khẩu đã hoàn toàn bị bỏ lại phía sau, bảo mật tuyệt đối!)*

                                ↓
    [BƯỚC 4: HIỂN THỊ RA MÀN HÌNH]
    Controller nhận cục DTO phẳng này, đẩy ra ngoài file HTML.
    Thymeleaf chỉ cần gọi lệnh nhẹ nhàng: th:text="${employee.departmentName}" là bảng danh sách hiện tên phòng ban đẹp đẽ ngay lập tức!
    */

}
