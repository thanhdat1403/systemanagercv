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
@Table(name = "cv_certificates")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CVCertificate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "cv_version_id",
            nullable = false
    )
    private CVVersion cvVersion;

    @Column(
            name = "certificate_name",
            nullable = false,
            length = 255
    )
    private String certificateName;

    @Column(
            name = "organization",
            length = 255
    )
    private String organization;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(
            name = "credential_id",
            length = 255
    )
    private String credentialId;

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