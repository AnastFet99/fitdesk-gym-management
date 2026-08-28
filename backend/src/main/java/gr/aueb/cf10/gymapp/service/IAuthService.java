package gr.aueb.cf10.gymapp.service;

import gr.aueb.cf10.gymapp.dto.AuthResponse;
import gr.aueb.cf10.gymapp.dto.LoginRequest;
import gr.aueb.cf10.gymapp.dto.RegisterRequest;

public interface IAuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
