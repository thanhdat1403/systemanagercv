package systemanagercv.example.systemanagercv;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Testpassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        String hash = encoder.encode(password);

        System.out.println("=================================");
        System.out.println("PASSWORD = " + password);
        System.out.println("HASH = " + hash);
        System.out.println("CHECK = " + encoder.matches(password, hash));
        System.out.println("=================================");
    }
}
