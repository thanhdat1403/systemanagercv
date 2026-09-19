package systemanagercv.example.systemanagercv.cv.dto.response;

import lombok.*;
import systemanagercv.example.systemanagercv.cv.enums.CvStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVListResponse {

    private Long id;

    private Long employeeId;

    private String employeeCode;

    private String employeeName;

    private String departmentCode;

    private String departmentName;

    private CvStatus status;

    private String currentVersion;
}
/* DTO này khớp trực tiếp với CVListProjection mà chúng ta đã tạo.
* Mapping:
* CVListProjection          CVListResponse
------------------------------------------------
getId()                   → id
getEmployeeId()           → employeeId
getEmployeeCode()         → employeeCode
getEmployeeName()         → employeeName
getDepartmentCode()       → departmentCode
getDepartmentName()       → departmentName
getStatus()               → status
getCurrentVersion()       → currentVersion*/