package systemanagercv.example.systemanagercv.common.auditor;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import systemanagercv.example.systemanagercv.security.CustomUserDetails;

import java.util.Optional;

/* Class giống như là mảnh ghép cuối cùng và quan trọng để kích hoạt thuộc tính @CreateBy và @LastModifiedby
 * 🌟 Class này thực hiện "implements AuditorAware<String>":
 * Cam kết với Spring Data JPA rằng: "Tôi là người chịu trách nhiệm đi tìm tên người thao tác (kiểu String)
 * để cung cấp cho các thẻ @CreatedBy và @LastModifiedBy".
 */
public class SpringSecurityAuditorAware implements AuditorAware<String> {
    /*HÀM TỰ ĐỘNG QUÉT VÀ TRẢ VỀ TÊN USERNAME ĐANG ĐĂNG NHẬP
    * Mỗi khi gọi lệnh .save(), Spring sẽ tự động nhảy vào hàm này để lấy tên*/
    @Override
    public Optional<String> getCurrentAuditor() {
        // 🔍 BƯỚC 1: Đột nhập vào "Két an toàn toàn cục" (SecurityContextHolder) của Spring Security
        // để lấy ra chiếc "Thẻ căn cước định danh" (Authentication) của phiên đăng nhập hiện tại.
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();
        // ❌ TRƯỜNG HỢP CHẶN 1: Nếu thẻ định danh trống rỗng (null) -> Nghĩa là hệ thống chưa từng có ai đăng nhập
        if (authentication == null) {
            return Optional.empty(); //Trả về rỗng, database sẽ ghi nhận cột created_by là NULL
        }
        // ❌ TRƯỜNG HỢP CHẶN 2: Người dùng ẩn danh (Anonymous) do chưa đăng nhập nhưng cố tình bấm link tự do
        if (authentication instanceof AnonymousAuthenticationToken){
            return Optional.empty();
        }
        // ❌ TRƯỜNG HỢP CHẶN 3: Thẻ định danh có tồn tại nhưng phiên làm việc này chưa hoàn tất xác thực (Bị lỗi đăng nhập)
        if (!authentication.isAuthenticated()){
            return Optional.empty();
        }

        // 🔓 BƯỚC 2: Khi đã vượt qua các chốt chặn an toàn -> Tiến hành lục soát lõi bên trong thẻ (Principal: hiệu trưởng)
        // để lôi ra thông tin gốc của chủ tài khoản.
        Object principal = authentication.getPrincipal();

        //TÌNH HUỐNG CHÍNH: Nếu cục thông tin này gốc này mang đúng cấu trúc file "CustomUserDetails"
        if (principal instanceof CustomUserDetails userDetails){
            //Bấm chuông báo cho BaseEntity: "Tôi tìm thấy rồi! Hãy lấy tên Username này ghi vào cột created_by/updated_by nhé!"
            return Optional.of(userDetails.getUsername());
        }
        //TÌNH HUỐNG PHỤ: Nếu hệ thống chạy qua Postman/API Basic Auth, đôi khi principal chỉ là một chuỗi chữ String đơn thuần
        if (principal instanceof String userName){
            return Optional.of(userName); // trả về chuỗi username đó luôn
        }
        // Nếu lục soát hết mà không khớp kiểu dữ liệu nào -> Trả về rỗng an toàn
        return Optional.empty();
    }
}
