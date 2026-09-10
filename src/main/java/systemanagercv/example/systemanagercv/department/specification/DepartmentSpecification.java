package systemanagercv.example.systemanagercv.department.specification;


import org.springframework.data.jpa.domain.Specification;
import systemanagercv.example.systemanagercv.department.entity.Departments;
import systemanagercv.example.systemanagercv.department.enums.DepartmentStatus;

//DepartmentSpecification sử dụng JPA Criteria API để xây dựng các điều kiện tìm kiếm động cho phòng ban (Departments)
// Khai báo final và private constructor để biến đây thành một Utility Class (Lớp tiện ích, không cho tạo đối tượng)
public final class DepartmentSpecification {

    private DepartmentSpecification(){}

    /**
     * 1. Bộ lọc tìm kiếm theo từ khóa (Mã phòng ban hoặc Tên phòng ban)
     */
    public static Specification<Departments> keyword(String keyword){

        // root: Đại diện cho bảng Department (đối tượng gốc để lấy các trường dữ liệu)
        // query: Đại diện cho toàn bộ câu lệnh truy vấn (giúp cấu hình DISTINCT, ORDER BY,...)
        // criteriaBuilder: Công cụ để xây dựng các điều kiện so sánh (LIKE, EQUAL, AND, OR,...)
        return (root, query, criteriaBuilder) -> {

            //Nếu người dùng không nhập từ khóa tìm kiếm (bị null hoặc chỉ gõ khoảng trắng)
            if (keyword == null || keyword.isBlank()){
                //Trả về một điều kiện trống luôn đúng -> Hệ thống tự bỏ qua k loc theo từ khóa nữa
                return criteriaBuilder.conjunction();
            }

            //Chuẩn hóa từ khóa: cắt khoảng trắng thừa, chuyển về chữ thường và bọc dấu % để dùng cho toán tử LIKE
            String value = "%" + keyword.trim().toLowerCase() + "%";

            //Tạo điều kiện: Tìm kiếm từ khóa xuất hiện ở cột mã phòng ban HOẶC cột tên phòng ban
            // Tương đương SQL: WHERE (LOWER(code) LIKE '%value%' OR LOWER(name) LIKE '%value%')
            return criteriaBuilder.or(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("code")), // Chuyển cột code dưới DB về chữ thường để so sánh không phân biệt hoa/thường
                            value
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("name")), // Chuyển cột name dưới DB về chữ thường để so sánh không phân biệt hoa/thường
                            value
                    )
            );
        };

    }

    /**
     * 2. Bộ lọc tìm kiếm theo Trạng thái của phòng ban (Ví dụ: ACTIVE, INACTIVE)
     */
    public static Specification<Departments> hasStatus(
            DepartmentStatus status
    ){

        return (root, query, criteriaBuilder) -> {

            // Nếu người dùng không chọn lọc theo trạng thái nào trên giao diện
            if (status == null){
                //Trả về điều kiện trống luôn đúng -> Bỏ qua không lọc theo trạng thái
                return criteriaBuilder.conjunction();
            }

            // Nếu có chọn trạng thái, thực hiện so sánh bằng (EQUAL) giữa dữ liệu cột status trong DB và giá trị truyền vào
            // Tương đương SQL: WHERE status = :status
            return criteriaBuilder.equal(
                    root.get("status"),
                    status
            );
        };
    }

    /**
     * 3. Bộ lọc mặc định: Loại bỏ các phòng ban đã bị xóa mềm (Soft Delete)
     */
    public static Specification<Departments> notDeleted(){

        return (root, query, criteriaBuilder) ->
            //Chỉ lấy những phòng ban có giá trị ở cột 'deleted' bằng FALSE
            //Tương đương SQL: WHERE deleted = false
            criteriaBuilder.isFalse(
                    root.get("deleted")
            );
    }
}
