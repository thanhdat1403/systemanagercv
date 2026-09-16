package systemanagercv.example.systemanagercv.cv.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import systemanagercv.example.systemanagercv.common.entity.BaseEntity;
import systemanagercv.example.systemanagercv.cv.enums.CvStatus;
import systemanagercv.example.systemanagercv.employee.entity.Employee;

@Entity
@Table(name = "employee_cvs")
@Getter
@Setter
// =========================================================================
// 🌟 CHÚ THÍCH CÔNG DỤNG CỦA NHÃN @NoArgsConstructor(access = AccessLevel.PROTECTED)
// =========================================================================
/*
 * 1. @NoArgsConstructor: Tự động tạo một hàm khởi tạo RỖNG không tham số (Constructor rỗng).
 * 👉 BẮT BUỘC PHẢI CÓ: Vì Hibernate/JPA ép buộc phải có hàm rỗng này thì nó mới bốc dữ liệu
 *                      từ database lên để dựng thành đối tượng Java được.
 *
 * 2. (access = AccessLevel.PROTECTED): Khóa hàm rỗng này lại bằng quyền bảo vệ "protected".
 * 👉 TÁC DỤNG BẢO MẬT: Chỉ cho phép Hibernate gọi ngầm dưới nền, CẤM TUYỆT ĐỐI các lập trình viên
 *                      ở tầng Service hay Controller tự ý gõ lệnh tạo đối tượng rỗng (new Departments()).
 *                      Điều này giúp ngăn chặn dữ liệu "bẩn", thiếu trường thông tin gây sập hệ thống (NullPointerException).
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmployeeCV extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "employee_id",
            nullable = false
    )
    private Employee employee;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "current_version_id"
    )
    private CVVersion currentVersion;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private CvStatus status = CvStatus.UPDATED;
}
