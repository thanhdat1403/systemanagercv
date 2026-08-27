package systemanagercv.example.systemanagercv.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter

/*
 * 🌟 @MappedSuperclass: Khai báo đây là "Lớp cha trừu tượng" chứa các cột chung.
 * Nó báo cho Hibernate biết: "Đừng tạo bảng nào tên là BaseEntity dưới DB nhé!
 * Hãy mang toàn bộ các thuộc tính bên dưới class này đi cấy (nhúng) vào các bảng con kế thừa nó".
 */
@MappedSuperclass
/*
 * 🌟 @EntityListeners(AuditingEntityListener.class): Kích hoạt "Robot lắng nghe ngầm".
 * Mỗi khi bạn gọi lệnh lưu (save) hoặc sửa đổi dữ liệu, con Robot này của Spring Data JPA
 * sẽ tự động giật mình tỉnh dậy để điền ngày giờ và tên người thao tác vào các ô trống dưới đây.
 */
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {  // Sử dụng từ khóa 'abstract' để cấm không cho ai tạo trực tiếp đối tượng BaseEntity (new BaseEntity() là sai)
    @Id // Đánh dấu đây là cột Khóa chính (Primary Key) của bảng dữ liệu
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Thiết lập giá trị ID tự động tăng (1, 2, 3...) dưới MariaDB/MySQL
    private Long id;

    @CreatedDate // 🌟 TỰ ĐỘNG: Nhặt ngày giờ hiện tại của hệ thống máy chủ để điền vào khi thêm mới dữ liệu lần đầu
    @Column(name = "created_date", nullable = false, updatable = false) // Tên cột SQL, bắt buộc nhập, cấm ghi đè khi sửa đổi (updatable = false)
    private LocalDateTime createdDate;

    @CreatedBy
    // 🌟 TỰ ĐỘNG: Nhặt cái 'username' của tài khoản đang đăng nhập trong hệ thống bảo mật (Spring Security) để ghi vào
    @Column(name = "created_by", updatable = false) // Lưu tên người tạo ra dòng dữ liệu này, cấm sửa đổi khi cập nhật thông tin phòng ban/user
    private String createdBy;

    @LastModifiedDate // 🌟 TỰ ĐỘNG: Cập nhật lại mốc thời gian hiện tại mỗi khi bạn thực hiện hành động SỬA dữ liệu
    @Column(name = "updated_date") // Tên cột lưu mốc thời gian cập nhật cuối cùng dưới DB
    private LocalDateTime updatedDate;

    @LastModifiedBy // 🌟 TỰ ĐỘNG: Nhặt 'username' của người vừa thực hiện hành động bấm nút SỬA dữ liệu để ghi đè vào
    @Column(name = "updated_by") // Tên cột lưu vết người chỉnh sửa cuối cùng
    private String updatedBy;

    @Column(name = "is_deleted", nullable = false) // Thiết lập cột kiểm tra trạng thái Xóa mềm (Soft Delete)
    private boolean deleted = false; // Mặc định dữ liệu mới tạo ra luôn luôn tồn tại (false = chưa bị xóa)
}

/*LUỒNG XỬ LÝ NGẦM:
* [Admin Đăng Nhập Tài Khoản: "user_hr"]
       ↓
[Tạo Phòng Ban Mới Ngoài Giao Diện] ➔ Bấm nút [Lưu Phòng Ban]
       ↓
[Tầng Service Gọi Lệnh]: departmentRepository.save(dept);
       ↓
[Robot AuditingEntityListener Giật Mình Thức Dậy]:
       │
       ├──> Bước 1: Quét thấy thẻ @CreatedDate ➔ Tự gọi lệnh `LocalDateTime.now()` nạp vào biến `createdDate`.
       │
       └──> Bước 2: Quét thấy thẻ @CreatedBy ➔ Chạy thẳng vào lõi Spring Security nhặt lấy chữ "user_hr" đang đăng nhập
                    ➔ Nạp thẳng vào biến `createdBy`.
       ↓
[Hibernate Xuống Lệnh Đổ Vào MariaDB]:
INSERT INTO departments (code, name, created_date, created_by, is_deleted)
VALUES ('TECH', 'Phòng Kỹ Thuật', '2026-08-21 15:30:00', 'user_hr', 0);
*/
