package systemanagercv.example.systemanagercv.cv.projection;

import systemanagercv.example.systemanagercv.cv.enums.CvStatus;

public interface CVListProjection {

    Long getId();

    Long getEmployeeId();

    String getEmployeeCode();

    String getEmployeeName();

    String getDepartmentCode();

    String getDepartmentName();

    CvStatus getStatus();

    String getCurrentVersion();
}
/*CVListProjection → CVListResponse
* Tên field giống nhau:
* projection                  response

id                →         id
employeeId        →         employeeId
employeeCode      →         employeeCode
employeeName      →         employeeName
departmentCode    →         departmentCode
departmentName    →         departmentName
status            →         status
currentVersion    →         currentVersion
* MapStruct tự mapping được*/