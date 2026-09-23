package systemanagercv.example.systemanagercv.cv.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import systemanagercv.example.systemanagercv.common.entity.BaseEntity;
import systemanagercv.example.systemanagercv.cv.enums.CvVersionStatus;

@Entity
@Table(
        name = "cv_versions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cv_versions_cv_version",
                        columnNames = {
                                "employee_cv_id",
                                "version"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class CVVersion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "employee_cv_id",
            nullable = false
    )
    private EmployeeCV employeeCV;

    @Column(
            name = "version",
            nullable = false,
            length = 20
    )
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private CvVersionStatus status = CvVersionStatus.DRAFT;

    @Column(
            name = "is_current",
            nullable = false
    )
    private Boolean isCurrent = false;
}
