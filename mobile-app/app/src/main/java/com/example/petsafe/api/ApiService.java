package com.example.petsafe.api;

import com.example.petsafe.models.ApiResponse;
import com.example.petsafe.models.ChangePasswordRequest;
import com.example.petsafe.models.Device;
import com.example.petsafe.models.DeviceRequest;
import com.example.petsafe.models.Geofence;
import com.example.petsafe.models.GeofenceRequest;
import com.example.petsafe.models.LoginRequest;
import com.example.petsafe.models.LoginResponse;
import com.example.petsafe.models.Pet;
import com.example.petsafe.models.PetRequest;
import com.example.petsafe.models.RegisterRequest;
import com.example.petsafe.models.UpdateProfileRequest;
import com.example.petsafe.models.User;
import com.example.petsafe.models.UserResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

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

    // ==================== PETS ENDPOINTS ====================

    /**
     * Endpoint para listar todos os pets do usuário
     * GET /api/v1/pets
     */
    @GET("pets")
    Call<ApiResponse<List<Pet>>> listPets(@Header("Authorization") String authorization);

    /**
     * Endpoint para obter detalhes de um pet específico
     * GET /api/v1/pets/:id
     */
    @GET("pets/{id}")
    Call<ApiResponse<Pet>> getPetDetails(
            @Header("Authorization") String authorization,
            @Path("id") Long petId
    );

    /**
     * Endpoint para criar um novo pet
     * POST /api/v1/pets
     */
    @POST("pets")
    Call<ApiResponse<Pet>> createPet(
            @Header("Authorization") String authorization,
            @Body PetRequest petRequest
    );

    /**
     * Endpoint para atualizar um pet existente
     * PATCH /api/v1/pets/:id
     */
    @PATCH("pets/{id}")
    Call<ApiResponse<Pet>> updatePet(
            @Header("Authorization") String authorization,
            @Path("id") Long petId,
            @Body PetRequest petRequest
    );

    /**
     * Endpoint para deletar um pet
     * DELETE /api/v1/pets/:id
     */
    @DELETE("pets/{id}")
    Call<ApiResponse<Boolean>> deletePet(
            @Header("Authorization") String authorization,
            @Path("id") Long petId
    );

    // ==================== DEVICES ENDPOINTS ====================

    /**
     * Endpoint para listar todos os dispositivos do usuário
     * GET /api/v1/devices
     */
    @GET("devices")
    Call<ApiResponse<List<Device>>> listDevices(@Header("Authorization") String authorization);

    /**
     * Endpoint para obter status de um dispositivo específico
     * GET /api/v1/devices/:id/status
     */
    @GET("devices/{id}/status")
    Call<ApiResponse<Device>> getDeviceStatus(
            @Header("Authorization") String authorization,
            @Path("id") Long deviceId
    );

    /**
     * Endpoint para obter detalhes de um dispositivo específico
     * GET /api/v1/devices/:id
     */
    @GET("devices/{id}")
    Call<ApiResponse<Device>> getDeviceDetails(
            @Header("Authorization") String authorization,
            @Path("id") Long deviceId
    );

    /**
     * Endpoint para criar um novo dispositivo
     * POST /api/v1/devices
     */
    @POST("devices")
    Call<ApiResponse<Device>> createDevice(
            @Header("Authorization") String authorization,
            @Body DeviceRequest deviceRequest
    );

    /**
     * Endpoint para atualizar um dispositivo existente
     * PATCH /api/v1/devices/:id
     */
    @PATCH("devices/{id}")
    Call<ApiResponse<Device>> updateDevice(
            @Header("Authorization") String authorization,
            @Path("id") Long deviceId,
            @Body DeviceRequest deviceRequest
    );

    /**
     * Endpoint para deletar um dispositivo
     * DELETE /api/v1/devices/:id
     */
    @DELETE("devices/{id}")
    Call<ApiResponse<Boolean>> deleteDevice(
            @Header("Authorization") String authorization,
            @Path("id") Long deviceId
    );

    // ==================== GEOFENCE ENDPOINTS ====================

    /**
     * Endpoint para obter o geofence do usuário
     * GET /api/v1/geofence
     */
    @GET("geofence")
    Call<ApiResponse<Geofence>> getGeofence(@Header("Authorization") String authorization);

    /**
     * Endpoint para criar um geofence
     * POST /api/v1/geofence
     */
    @POST("geofence")
    Call<ApiResponse<Geofence>> createGeofence(
            @Header("Authorization") String authorization,
            @Body GeofenceRequest geofenceRequest
    );

    /**
     * Endpoint para atualizar o geofence
     * PATCH /api/v1/geofence
     */
    @PATCH("geofence")
    Call<ApiResponse<Geofence>> updateGeofence(
            @Header("Authorization") String authorization,
            @Body GeofenceRequest geofenceRequest
    );

    /**
     * Endpoint para deletar o geofence
     * DELETE /api/v1/geofence
     */
    @DELETE("geofence")
    Call<ApiResponse<Boolean>> deleteGeofence(@Header("Authorization") String authorization);

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
