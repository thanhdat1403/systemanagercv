package systemanagercv.example.systemanagercv.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "{validation.username.required}")
    private String username;

    @NotBlank(message = "{validation.password.required}")
    private String password;
}
