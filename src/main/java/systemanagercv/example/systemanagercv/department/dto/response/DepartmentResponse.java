package systemanagercv.example.systemanagercv.department.dto.response;

import lombok.Getter;
import lombok.Setter;
import systemanagercv.example.systemanagercv.department.enums.DepartmentStatus;

import java.time.LocalDateTime;

@Getter
@Setter
public class DepartmentResponse {

    //Dữ liêu trả ra giao diện/API

    private Long id;

    private String code;

    private String name;

    private String description;

    private DepartmentStatus status;

    private LocalDateTime createdDate;

    private String createdBy;

    private LocalDateTime updatedDate;

    private String updatedBy;
}