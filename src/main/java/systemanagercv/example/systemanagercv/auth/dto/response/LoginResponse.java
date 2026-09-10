package systemanagercv.example.systemanagercv.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LoginResponse {

    private String accessToken;

    private String tokenType;

    private long expiresIn;

    private String username;

    private List<String> roles;
}

/*Mục tiêu:
* {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9....",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "username": "admin",
    "roles": [
        "ADMIN"
    ]
}*/