package systemanagercv.example.systemanagercv.employee.authorization;


// File này giúp mô tả phạm vi được phép truy cập
/**
 *  * Xác định phạm vi Employee mà một user được phép truy cập.
 *  * Enum này KHÔNG trực tiếp kiểm tra quyền.
 *  * Nó chỉ mô tả "phạm vi" sau khi hệ thống đã xác định role của user.
 *  * Ví dụ:
 *  * ADMIN * → ALL
 *  * HR * → ALL
 *  * TECH_LEAD * → DEPARTMENT
 *  * EMPLOYEE * → SELF */
public enum EmployeeAccessType {

    ALL,

    DEPARTMENT,

    SELF
}
