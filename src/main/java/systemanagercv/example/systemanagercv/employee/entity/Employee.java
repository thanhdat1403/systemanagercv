package systemanagercv.example.systemanagercv.employee.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime; //Thư viện thời gian của Java
import jakarta.persistence.*; // Sử dụng thư viện jakarta cho Spring Boot đời cao
import systemanagercv.example.systemanagercv.department.entity.Departments;
import systemanagercv.example.systemanagercv.user.entity.User;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "employee_code", nullable = false, unique = true, length = 50)
    private String employeeCode;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(length = 255)
    private String email;

    @Column(length = 30)
    private String phone;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Departments department;

    @Column(length = 100)
    private String position;

    @Column(name = "job_title", length = 150)
    private String jobTitle;

    @Column(name = "join_date")
    private LocalDate joinDate;

    @Column(nullable = false, length = 30)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
