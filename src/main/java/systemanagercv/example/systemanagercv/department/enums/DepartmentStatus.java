package systemanagercv.example.systemanagercv.department.enums;


public enum DepartmentStatus {
    /*Thay cho việc hard-code: "ACTIVE"
    "INACTIVE"*/
    ACTIVE("Hoạt động"),
    INACTIVE("Không hoạt động");

    private final String description;
    DepartmentStatus(String description) {
        this.description = description;
    }

    //Để in dữ liệu ra view sạch hơn
    public String getDescription() {
        return description;
    }
}
