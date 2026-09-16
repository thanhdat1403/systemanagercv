package systemanagercv.example.systemanagercv.cv.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import systemanagercv.example.systemanagercv.common.entity.BaseEntity;

@Entity
@Table(name = "cv_profiles")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CVProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "cv_version_id",
            nullable = false
    )
    private CVVersion cvVersion;

    @Column(
            name = "headline",
            length = 255
    )
    private String headline;

    @Column(
            name = "address",
            length = 500
    )
    private String address;

    @Column(
            name = "avatar_url",
            length = 1000
    )
    private String avatarUrl;

    @Column(
            name = "career_objective",
            columnDefinition = "TEXT"
    )
    private String careerObjective;
}