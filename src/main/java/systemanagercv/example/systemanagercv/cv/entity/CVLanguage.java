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
@Table(name = "cv_languages")
@Getter
@Setter
@NoArgsConstructor
public class CVLanguage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "cv_version_id",
            nullable = false
    )
    private CVVersion cvVersion;

    @Column(
            name = "language_code",
            nullable = false,
            length = 10
    )
    private String languageCode;

    @Column(
            name = "language_name",
            nullable = false,
            length = 100
    )
    private String languageName;

    @Column(
            name = "proficiency",
            length = 50
    )
    private String proficiency;

    @Column(
            name = "sort_order",
            nullable = false
    )
    private Integer sortOrder = 0;
}