package systemanagercv.example.systemanagercv.auth.service;

import systemanagercv.example.systemanagercv.auth.dto.request.LoginRequest;
import systemanagercv.example.systemanagercv.auth.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}
