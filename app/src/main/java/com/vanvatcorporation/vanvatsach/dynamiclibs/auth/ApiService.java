package com.vanvatcorporation.vanvatsach.dynamiclibs.auth;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @FormUrlEncoded
    @POST("/api/login")
    // Call<ApiResponse<User>> login(@Body LoginRequest request);
    Call<ApiResponse<User>> login(@Field("accountUsername") String username, @Field("accountPassword") String password);

    // Placeholder for Google Login - Backend implementation required
    @POST("/api/login/google")
    Call<ApiResponse<User>> loginWithGoogle(@Body GoogleLoginRequest request);

    @POST("/api/register")
    Call<ApiResponse<User>> register(@Body RegisterRequest request);

    @GET("/api/profile")
    Call<ApiResponse<User>> getProfile();

    @POST("/api/logout")
    Call<ApiResponse<Void>> logout();
}
