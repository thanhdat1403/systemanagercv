package systemanagercv.example.systemanagercv.auth.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Service // Đánh dấu lớp này là một Service để Spring quản lý và cho phép nhúng vào Controller/Filter bảo mật
@RequiredArgsConstructor
public class JwtService {

    /**
     * Secret key dùng để ký và xác thực JWT.
     */
    //Tiêm (Inject) chuỗi mã bí mật dùng để ký tên đóng dấu vé từ file cấu hình application.properties
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Thời gian sống của JWT.
     *
     * Đơn vị: milliseconds.
     */
    //Tiêm thông số thời gian hết hạn của vé (tính bằng mili-giây, ví dụ 1 ngày = 86400000)
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Tạo JWT access token từ UserDetails.
     */
    /**
     * CHỨC NĂNG 1: ĐÚC VÉ (Tạo JWT access token từ thông tin UserDetails sau khi đăng nhập thành công)
     */

    public String generateToken(UserDetails userDetails) {

        // 1.1. Lấy toàn bộ danh sách Quyền (Role) của người dùng hiện tại
        // Ví dụ gốc: "ROLE_ADMIN", "ROLE_EMPLOYEE" -> .replace sẽ cắt chữ "ROLE_" đi thành "ADMIN", "EMPLOYEE" cho gọn
        List<String> roles = userDetails
                .getAuthorities()
                .stream()
                .map(authority -> authority
                        .getAuthority()
                        .replace("ROLE_", ""))
                .toList();

        // Thời điểm token được tạo
        Date issuedAt = new Date();

        // Tính toán thời gian hết hạn của vé = Thời gian hiện tại + Khoảng thời gian sống của vé
        Date expirationDate =
                new Date(issuedAt.getTime() + expiration);

        // Sử dụng thư viện JWTs để bắt đầu xây dựng cấu trúc của tấm vé JWT
        return Jwts.builder()
                .subject(userDetails.getUsername()) // Ghi tên đăng nhập (Username) vào vé
                .claim("roles", roles)        // Ghi thêm danh sách Quyền (Role) vào ruột vé
                .issuedAt(issuedAt)                 // Đánh dấu ngày phát hành vé
                .expiration(expirationDate)         // Đóng dấu hạn sử dụng của vé
                .signWith(getSigningKey())          // KÝ TÊN BẢO MẬT bằng chìa khóa bí mật (Chống chỉnh sửa lén)
                .compact();
    }

    /**
     * CHỨC NĂNG 2: ĐỌC TÊN (Lấy username ghi trên tấm vé JWT)
     */
    public String extractUsername(String token) {
        // Giải mã toàn bộ tấm vé và bốc riêng trường 'Subject' (Username) ra ngoài
        return extractAllClaims(token)
                .getSubject();
    }

    /**
     * CHỨC NĂNG 3: ĐỌC QUYỀN (Lấy danh sách quyền ghi trên tấm vé JWT)
     */
    /**
     * Lấy danh sách role từ JWT.
     */

    public List<String> extractRoles(String token) {
        // Giải mã tấm vé
        Claims claims = extractAllClaims(token);
        // Bốc trường dữ liệu tự gán tên là "roles" chuyển về cấu trúc dạng mảng (List) trong Java
        return claims.get("roles", List.class);
    }
    /**
     * CHỨC NĂNG 4: SO QUÁT VÉ (Kiểm tra xem tấm vé JWT gửi lên có trùng khớp với User đang truy cập không)
     */
    /**
     * Kiểm tra JWT có hợp lệ với UserDetails hay không.
     */
    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {
        String username = extractUsername(token); // Tự thò tay đọc tên ghi trên vé gửi lên

        // Vé được coi là HỢP LỆ nếu: Tên trên vé khớp 100% với tên người đang truy cập VÀ vé đó PHẢI CHƯA HẾT HẠN
                return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);

    }

    /**
     * CHỨC NĂNG 5: KIỂM TRA HẠN SỬ DỤNG (Kiểm tra xem vé JWT đã quá hạn chưa)
     */

    private boolean isTokenExpired(String token) {
        // Đọc hạn sử dụng trên vé, kiểm tra xem mốc thời gian đó có đứng TRƯỚC (.before) thời gian hiện tại hay không
        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }

    /**
     * CHỨC NĂNG LÕI 1: SO CHỮ KÝ (Đọc và giải mã toàn bộ ruột gan Claims nằm trong vé JWT)
     * Lưu ý: Nếu tấm vé bị client cố tình sửa chữ, hàm parseSignedClaims này sẽ lập tức ném lỗi báo động chặn đứng lại.
     */
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey()) // Đưa chìa khóa bí mật vào để đối chiếu chữ ký gốc
                .build()
                .parseSignedClaims(token) // Tiến hành bóc tách giải mã chuỗi token
                .getPayload();               // Lấy ra phần ruột chứa thông tin (Payload) của tấm vé
    }

    /**
     * CHỨC NĂNG LÕI 2: ĐÚC CHÌA KHÓA KÝ TÊN (Tạo mã SecretKey chuẩn bảo mật từ chuỗi cấu hình bí mật)
     */
    private SecretKey getSigningKey() {
        // Giải mã chuỗi secret từ định dạng văn bản BASE64 thành mảng các byte thô
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        // Dùng thuật toán HMAC-SHA để đúc mảng byte đó thành chiếc chìa khóa ký tên bảo mật chuẩn của hệ thống
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /*
    * Lấy thời gian sống của access token
    * */

    public long getExpiration() {
        return expiration;
    }
}
