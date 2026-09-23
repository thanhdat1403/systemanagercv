package systemanagercv.example.systemanagercv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SystemanagercvApplication {

    public static void main(String[] args) {

        System.out.println("JWT_SECRET EXISTS = " + (System.getenv("JWT_SECRET") != null));
        System.out.println("JWT_EXPIRATION ENV = " + System.getenv("JWT_EXPIRATION"));
        System.out.println("DB_URL ENV = " + System.getenv("DB_URL"));
        System.out.println("DB_USERNAME ENV = " + System.getenv("DB_USERNAME"));
        System.out.println("DB_PASSWORD EXISTS = " + (System.getenv("DB_PASSWORD") != null));

        SpringApplication.run(SystemanagercvApplication.class, args);
    }
}