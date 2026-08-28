package systemanagercv.example.systemanagercv.department.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import systemanagercv.example.systemanagercv.common.entity.BaseEntity;
import systemanagercv.example.systemanagercv.department.enums.DepartmentStatus;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Departments extends BaseEntity {

    @Column(name = "code", length = 50, unique = true, nullable = false)
    private String code;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)//Enum:Ép hệ thống phải lưu dữ liệu dưới dạng chữ văn bản rõ ràng (như định dạng trong DepartmentStatus)
    //Giúp dữ liệu database trực quan, dễ đọc và giữ cho ứng dụng của bạn an toàn tuyệt đối, không lo bị lỗi đổi thứ tự code sau này
    //@Enumerated(EnumType.STRING) sẽ tự lưu: ACTIVE, INACTIVE vào cột status
    @Column(name = "status", length = 30, nullable = false)
    private DepartmentStatus status = DepartmentStatus.ACTIVE;
}