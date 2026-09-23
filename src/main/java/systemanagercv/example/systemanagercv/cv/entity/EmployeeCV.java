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

@NoArgsConstructor
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
