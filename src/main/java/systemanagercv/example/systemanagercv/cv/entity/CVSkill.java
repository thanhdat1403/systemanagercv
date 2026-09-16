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

@Entity
@Table(name = "cv_skills")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CVSkill extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "cv_version_id",
            nullable = false
    )
    private CVVersion cvVersion;

    @Column(
            name = "skill_name",
            nullable = false,
            length = 255
    )
    private String skillName;

    @Column(
            name = "skill_level",
            length = 50
    )
    private String skillLevel;

    @Column(
            name = "description",
            columnDefinition = "TEXT"
    )
    private String description;

    @Column(
            name = "sort_order",
            nullable = false
    )
    private Integer sortOrder = 0;
}