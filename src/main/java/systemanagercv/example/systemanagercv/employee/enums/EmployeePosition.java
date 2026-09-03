package systemanagercv.example.systemanagercv.employee.enums;

public enum EmployeePosition {
    /*position đây cũng là cụng cố định nên phải dùng Enum
    * NHAN_VIEN
    TRUONG_NHOM
    PHO_PHONG
    TRUONG_PHONG
    QUAN_LY
    * Tránh cho việc hard-code*/
    NHAN_VIEN("Nhân viên"),
    TRUONG_NHOM("Trưởng nhóm"),
//    PHO_PHONG("Phó phòng"),
    TRUONG_PHONG("Trưởng phòng"),
    QUAN_LY("Quản lý");

    private final String description;
    EmployeePosition(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
