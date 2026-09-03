package systemanagercv.example.systemanagercv.user.specification;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import systemanagercv.example.systemanagercv.role.entity.Role;
import systemanagercv.example.systemanagercv.role.entity.UserRole;
import systemanagercv.example.systemanagercv.user.dto.request.UserSearchRequest;
import systemanagercv.example.systemanagercv.user.entity.User;

import java.util.ArrayList;
import java.util.List;

//Đây là file Dynamic Query: Hiểu đơn giản Dynamic Query = câu truy vấn được tự động thay đổi tùy theo điều kiện ng dùng nhập
//Khai báo final và private constructor để ngăn việc khởi tạo đối tượng (Utility class)
public final class UserSpecification {

    private UserSpecification() {
    }

    /*
    * Hàm chính để tạo ra cấu trúc tìm kiếm động dựa trên dữ liệu người dùng gửi lên (UserSearchRequest)
    * */
    public static Specification<User> search(UserSearchRequest request){
        //root: Đại diện cho bảng User (đối tượng gốc để lấy các trường dữ liệu)
        // query: Đại diện cho toàn bộ câu lệnh truy vấn (giúp cấu hình DISTINCT, ORDER BY,..)
        // criteriaBuilder: Công cụ để xây dựng các điều kiện so sánh (LIKE, EQUAL, AND, OR,...)
        return ((root, query, criteriaBuilder) ->  {

            //Danh sách chứa tất cả các điều kiện lọc (WHERE) sẽ được kết hợp bằng toán tử AND
            List<Predicate> predicates = new ArrayList<>();

            //1. Luôn thêm điều kiện lọc tài khoản chưa bị xóa
            addNotDeleteCondition(
                    root,
                    criteriaBuilder,
                    predicates
            );

            //2. Thêm điều kiện tìm theo từ khóa (Username hoặc Email) nếu có nhập
            addKeywordCondition(
                    root,
                    criteriaBuilder,
                    predicates,
                    request.getKeyword()
            );

            //3. Thêm điều kiện lọc theo ID quyền (Role ID) nếu có chọn
            addRoleCondition(
                    root,
                    query,
                    criteriaBuilder,
                    predicates,
                    request.getRoleId()
            );

            //4. Thêm điều kiện lọc theo trạng thái kích hoạt (Enabled) nếu có chọn
            addEnabledCondition(
                    root,
                    criteriaBuilder,
                    predicates,
                    request.getEnabled()
            );

            //Gộp tất cả các điều kiện trong danh sách thành một câu lệnh có dạng:
            //WHERE dieu_kien_1 AND dieu_kien_2 AND dieu_kien_3...
            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );

        });
    }

    /**
     * Hàm xử lý: Chỉ lấy những User chưa bị xóa (deleted = false)
     */
    private static void addNotDeleteCondition(
            Root<User> root,
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates
    ){
        //Tương đương trong SQL: WHERE is_delete = false
        predicates.add(criteriaBuilder.isFalse(root.get("deleted")));
    }

    /**
     * Hàm xử lý: Tìm kiếm User theo từ khóa (không phân biệt chữ hoa chữ thường)
     */
    private static void addKeywordCondition(
            Root<User> root,
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates,
            String keyword
    ){
        //Nếu người dùng không nhập từ khóa, hoặc chỉ nhập khoảng trắng -> Bỏ qua không lọc
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }

        //Chuẩn hóa từ khóa: xóa khoảng trắng thừa, chuyển về chữ thường và bọc trong dấu % để tìm kiếm chứa chuỗi (LIKE)
        //Ví dụ: "Admin " -> "%admin%"
        String searchKeyword = "%" + keyword.trim().toLowerCase() + "%";
        //Xử lý từ khóa người dùng nhập: keyword.trim().toLowerCase(): Nếu người dùng nhập vào ô tìm kiếm là "Admin" hoặc "ADMIN", hệ thống sẽ tự động chuyển nó về thành "admin".

        //Tạo điều kiện: LOWER(username) LIKE '%từ_khóa%' , và tác dụng của lower giúp san phẳng tất cả về chữ thường để máy tính không bị nhầm lẫn giữa chữ hoa và chữ thường
        //VD:Nếu KHÔNG có lower: Bạn gõ từ khóa là alex, máy tính đi tìm trong bảng thấy chữ Alex (chữ A viết hoa). Vì máy tính rất máy móc, nó thấy A khác a nên nó báo không tìm thấy.
        //Nếu CÓ lower: Máy tính chuyển chữ Alex trong bảng thành alex. Lúc này alex so sánh với alex là khớp nhau 100%. Kết quả: Máy tính tìm thấy người dùng này.
        Predicate usernamePredicate =
                criteriaBuilder.like(
                        criteriaBuilder.lower(
                                root.get("username") //root(gốc rễ), root.get("username") đi thẳng vào bảng dữ liệu và lấy ra cột tên là username
                        ),
                        searchKeyword
                );

        //Tạo điều kiện: LOWER(email) LIKE '%từ_khóa%'
        Predicate emailPredicate =
                criteriaBuilder.like(
                        criteriaBuilder.lower(
                            root.get("email")
                        ),
                        searchKeyword
                );

        //Kết hợp 2 điều kiện bằng toán tử OR (Tìm thấy ở username HOẶC email đều được chấp nhận)
        //Tương đương SQL: AND (LOWER(username) LIKE ... OR LOWER(email) LIKE ...)
        predicates.add(
                criteriaBuilder.or(
                        usernamePredicate,
                        emailPredicate
                )
        );
    }

    /**
     * Hàm xử lý: Lọc người dùng theo Quyền (Role) cụ thể thông qua bảng trung gian (UserRole)
     */
    private static void addRoleCondition(
            Root<User> root,
            CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates,
            Long roleId
    ){
        //Nếu không chọn quyền nào cụ thể -> Bỏ qua không lọc
        if (roleId == null) {
            return;
        }

        //Thực hiện INNER JOIN từ bảng User sang bảng trung gian UserRole
        //Tương đương SQL: INNER JOIN user_role ON user.id = user_role.user_id
        Join<User, UserRole> userRoleJoin =
                root.join(
                        "userRoles",
                        JoinType.INNER
                );

        //Tiếp tục INNER JOIN từ bảng trung gian UserRole sang bảng gốc Role
        //Tương đương SQL: INNER JOIN role ON user_role.role_id = role.id
        Join<UserRole, Role> roleJoin =
                userRoleJoin.join(
                        "role",
                        JoinType.INNER
                );

        //Thêm điều kiện so sánh ID của quyền bằng với ID truyền vào
        //Tương đương SQL: AND role.id = :roleId
        predicates.add(
                criteriaBuilder.equal(
                        roleJoin.get("id"),
                        roleId
                )
        );

        //Loại bỏ các bản ghi trùng lặp trong kết quả trả về (Vì một User có thể thảo mãn nhiều điều kiện Join)
        // Tương đương SQL: SELECT DISTINCT ...
        query.distinct(true);
    }

    /**
     * Hàm xử lý: Lọc người dùng theo trạng thái kích hoạt (Hoạt động / Bị khóa)
     */
    private static void addEnabledCondition(
            Root<User> root,
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates,
            Boolean enabled
    ){
        //Nếu không chọn trạng thái nào -> Bỏ qua không lọc
        if (enabled == null) {
            return;
        }

        //Tương đương SQL: AND enabled = true (hoặc false)
        predicates.add(
                criteriaBuilder.equal(
                        root.get("enabled"),
                        enabled
                )
        );
    }
}
