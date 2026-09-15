package systemanagercv.example.systemanagercv.employee.authorization;


import lombok.Builder;
import lombok.Getter;

/* Được phép truy cập tất cả Employee.
* Áp dụng:
* ADMIN - HR /ALL
* Chỉ được phép truy cập Employee * thuộc cùng Department với người đăng nhập.
* Áp dụng: * - TECH_LEAD / DEPARTMENT
*
* Chỉ được phép truy cập chính Employee * đang đăng nhập. * * Áp dụng: * - EMPLOYEE / SELF*/
@Getter
@Builder
public class EmployeeAccessScope {

    /* Loại phạm vi truy cập.
    * Có thể là:
    * ALL
    * DEPARTMENT
    * SELF*/
    private final EmployeeAccessType accessType;

    /*DEPARTMENT là User được phép truy cập.
    Chỉ sử dụng khi:
    accessType =  DEPARTMENT
    Ví dụ:
    TECH_LEAD thuộc departmentId = 5
    -> departmentId = 5
    * */
    private final Long departmentId;


    /** * Employee mà user được phép truy cập. *
     * * Chỉ sử dụng khi: *
     * * accessType = SELF *
     * * Ví dụ: * EMPLOYEE có employeeId = 20 * → employeeId = 20 */
    private final Long employeeId;
}
/*User đăng nhập
      ↓
JWT xác định username + role
      ↓
EmployeeAuthorizationService
      ↓
Xác định phạm vi Employee
      ↓
EmployeeAccessScope
      ↓
EmployeeAccessType
      ├── ALL         → ADMIN, HR
      ├── DEPARTMENT  → TECH_LEAD
      └── SELF        → EMPLOYEE*/