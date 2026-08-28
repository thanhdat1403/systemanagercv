package systemanagercv.example.systemanagercv.employee.dto.response;

import lombok.Builder;
import lombok.Getter;
import systemanagercv.example.systemanagercv.employee.enums.EmployeePosition;
import systemanagercv.example.systemanagercv.employee.enums.EmployeeStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Getter
@Builder //Nhãn này giúp tạo ra đối tượng Response mới và gán dữ liệu cho nó theo phong cách chuỗi cực kỳ sạch sẽ và dễ đọc mà k cần dùng hàm set hay tạo constructor dài dòng
//NV chính của file này là : bảo mật thông tin , ẩn đi dữ liệu nhạy cảm
public class EmployeeResponse {

        //Nhóm 1: lấy thông tin từ bảng users
        //Giúp giao diện nhận biết nhân viên này đang liêt kết với tài khoản đăng nhập nào trong hệ thống bảo mật
        private Long userId;
        private String username;
        private String email;

        //Nhóm 2: thông tin lấy từ bảng employees
        private Long id;
        private String employeeCode; //Mã nhân viên (VD: NV001)
        private String fullName; // Họ và tên
        private String phone; // Sdth

        /*Điều quan trọng là ở 2 dòng này:
        * Chính là áp dụng theo quy định Enum
        * Khi trả về Enum trong DTO nếu entity dùng enum -> trả thêm description/message của enum.*/
        private EmployeePosition position; //Chức vụ: Trưởng nhóm,...
        private String positionDescription;

        private String jobTitle; //Tiêu đề công việc cụ thể
        private LocalDate joinDate; //Ngày vào cty

        /*Điều quan trọng là ở 2 dòng này:
         * Chính là áp dụng theo quy định Enum
         * Khi trả về Enum trong DTO nếu entity dùng enum -> trả thêm description/message của enum.*/
        private EmployeeStatus status; //Trạng thái: đang làm việc or nghỉ việc
        private String statusDescription;


        //Nhóm 3: lấy thông tin từ bảng departments
        private Long departmentId;
        private String departmentCode; //VD: IT, HR,..
        private String departmentName; // phòng cntt,...

        //Nhóm 4: lấy thông tin nhật ký lịch sử (kế thừa từ BaseEntity)
        private LocalDateTime createdDate;
        private String createdBy;
        private LocalDateTime updatedDate;
        private String updatedBy;
    }
