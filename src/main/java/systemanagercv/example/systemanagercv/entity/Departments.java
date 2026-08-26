package systemanagercv.example.systemanagercv.entity;

import jakarta.persistence.*; // Sử dụng thư viện jakarta cho Spring Boot đời cao
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime; //Thư viện thời gian của Java

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor //Tự động tạo contructor k có tham số
@AllArgsConstructor // Tự động tạo Constructor có đầy đủ tham số
public class Departments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "code", length = 50, unique = true, nullable = false)
    private String code;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", length = 30, nullable = false)
    private String status = "ACTIVE"; // Giá trị mặc định khi tạo mới

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========================================================
    // TỰ ĐỘNG SINH THỜI GIAN THEO LIFECYCLE CỦA JPA
    // ========================================================

    @PrePersist
    protected void onCreate() {
        // Tự động điền ngày giờ hiện tại khi gọi lệnh lưu mới (INSERT)
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        // Tự động điền ngày giờ hiện tại khi gọi lệnh cập nhật (UPDATE)
        this.updatedAt = LocalDateTime.now();
    }
}
