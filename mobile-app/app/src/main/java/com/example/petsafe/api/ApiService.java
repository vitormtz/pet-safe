package com.example.petsafe.api;

import com.example.petsafe.models.ChangePasswordRequest;
import com.example.petsafe.models.LoginRequest;
import com.example.petsafe.models.LoginResponse;
import com.example.petsafe.models.RegisterRequest;
import com.example.petsafe.models.UpdateProfileRequest;
import com.example.petsafe.models.User;
import com.example.petsafe.models.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;

public interface ApiService {

    /**
     * Endpoint para login de usuário
     * POST /api/v1/auth/login
     */
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    /**
     * Endpoint para cadastro de novo usuário
     * POST /api/v1/auth/register
     */
    @POST("auth/register")
    Call<User> register(@Body RegisterRequest registerRequest);

    /**
     * Endpoint para renovar access token usando refresh token
     * POST /api/v1/auth/refresh
     */
    @POST("auth/refresh")
    Call<LoginResponse> refreshToken(@Body RefreshTokenRequest refreshTokenRequest);

    /**
     * Endpoint para obter dados do usuário logado
     * GET /api/v1/me
     */
    @GET("me")
    Call<UserResponse> getUserProfile(@Header("Authorization") String authorization);

    /**
     * Endpoint para atualizar perfil do usuário logado
     * PATCH /api/v1/me
     */
    @PATCH("me")
    Call<UserResponse> updateUserProfile(
            @Header("Authorization") String authorization,
            @Body UpdateProfileRequest updateProfileRequest
    );

    /**
     * Endpoint para alterar senha do usuário logado
     * PATCH /api/v1/me/password
     */
    @PATCH("me/password")
    Call<Void> changePassword(
            @Header("Authorization") String authorization,
            @Body ChangePasswordRequest changePasswordRequest
    );

    /**
     * Classe auxiliar para request de refresh token
     */
    class RefreshTokenRequest {
        private String refresh_token;

        public RefreshTokenRequest(String refreshToken) {
            this.refresh_token = refreshToken;
        }

        public String getRefreshToken() {
            return refresh_token;
        }

        public void setRefreshToken(String refreshToken) {
            this.refresh_token = refreshToken;
        }
    }
}
