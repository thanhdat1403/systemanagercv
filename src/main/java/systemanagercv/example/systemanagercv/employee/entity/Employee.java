package systemanagercv.example.systemanagercv.employee.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import systemanagercv.example.systemanagercv.common.entity.BaseEntity;
import systemanagercv.example.systemanagercv.department.entity.Departments;
import systemanagercv.example.systemanagercv.employee.enums.EmployeePosition;
import systemanagercv.example.systemanagercv.employee.enums.EmployeeStatus;
import systemanagercv.example.systemanagercv.user.entity.User;

import java.time.LocalDate;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//Employee cũng cho thừa kế theo tiêu chuẩn đã tạo file BaseEntity nên không phải tạo các cột private id,created_date,created_by,...
public class Employee extends BaseEntity {

    // =====================================================
    // TÀI KHOẢN ĐĂNG NHẬP
    // =====================================================

    @OneToOne
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private User user;


    // =====================================================
    // THÔNG TIN NHÂN VIÊN
    // =====================================================

    @Column(
            name = "employee_code",
            nullable = false,
            unique = true,
            length = 50
    )
    private String employeeCode;

    @Column(
            name = "full_name",
            nullable = false,
            length = 255
    )
    private String fullName;

    @Column(length = 255)
    private String email;

    @Column(length = 30)
    private String phone;


    // =====================================================
    // PHÒNG BAN
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "department_id",
            nullable = false
    )
    private Departments department;


    // =====================================================
    // CHỨC VỤ
    // =====================================================

    /*position đây cũng là cụng cố định nên phải dùng Enum
    * NHAN_VIEN
    TRUONG_NHOM
    PHO_PHONG
    TRUONG_PHONG
    QUAN_LY*/
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private EmployeePosition position;


    // =====================================================
    // CHỨC DANH
    // =====================================================

    @Column(
            name = "job_title",
            length = 150
    )
    private String jobTitle;


    // =====================================================
    // NGÀY VÀO LÀM
    // =====================================================

    @Column(name = "join_date")
    private LocalDate joinDate;


    // =====================================================
    // TRẠNG THÁI
    // =====================================================

    @Enumerated(EnumType.STRING) //Enum:Ép hệ thống phải lưu dữ liệu dưới dạng chữ văn bản rõ ràng (như định dạng trong EmployeeStatus)
    //Giúp dữ liệu database trực quan, dễ đọc và giữ cho ứng dụng của bạn an toàn tuyệt đối, không lo bị lỗi đổi thứ tự code sau này
    @Column(
            nullable = false,
            length = 30
    )
    private EmployeeStatus status = EmployeeStatus.ACTIVE;
}