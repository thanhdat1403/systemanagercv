package systemanagercv.example.systemanagercv.cv.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import systemanagercv.example.systemanagercv.common.entity.BaseEntity;

import java.time.LocalDate;

@Entity
@Table(name = "cv_projects")
@Getter
@Setter
@NoArgsConstructor
public class CVProject extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "cv_version_id",
            nullable = false
    )
    private CVVersion cvVersion;

    @Column(
            name = "project_name",
            nullable = false,
            length = 255
    )
    private String projectName;

    @Column(
            name = "role",
            length = 255
    )
    private String role;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(
            name = "description",
            columnDefinition = "TEXT"
    )
    private String description;

    @Column(
            name = "technologies",
            columnDefinition = "TEXT"
    )
    private String technologies;

    @Column(name = "team_size")
    private Integer teamSize;

    @Column(
            name = "responsibilities",
            columnDefinition = "TEXT"
    )
    private String responsibilities;

    @Column(
            name = "sort_order",
            nullable = false
    )
    private Integer sortOrder = 0;
}