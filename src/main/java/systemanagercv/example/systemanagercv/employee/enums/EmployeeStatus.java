package systemanagercv.example.systemanagercv.employee.enums;

public enum EmployeeStatus {

    /*Thay cho việc hard-code: "ACTIVE"
    "INACTIVE"*/
    ACTIVE("Hoạt động"),
    INACTIVE("Không hoạt động");

    private final String description;

    EmployeeStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}