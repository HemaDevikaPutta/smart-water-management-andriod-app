package com.example.smartwatermanagement

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/auth/register")
    suspend fun registerUser(
        @Query("name") name: String,
        @Query("email") email: String,
        @Query("password") password: String
    ): Response<RegisterResponse>

    @POST("api/auth/login")
    suspend fun loginUser(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>


    @GET("api/waterlevel/current")
    suspend fun getCurrentWaterLevel(
        @Header("Authorization") authHeader: String,
    ): Response<WaterLevelResponse>

  @GET("api/temperature/current")
    suspend fun getCurrentTemprature(
        @Header("Authorization") authHeader: String,
    ): Response<CurrentTemperatureResponse>

    @GET("api/humidity/current")
    suspend fun getCurrentHumidity(
        @Header("Authorization") authHeader: String,
    ): Response<CurrentHumidityResponse>
    @GET("/api/humidity/recent")
    suspend fun getCheckLeakage(
        @Header("Authorization") authHeader: String,
    ): Response<List<HumidityRecord>> // List of HumidityRecord
    // The response type should be checkLeakageResponse

    data class HumidityRecord(
        val id: Int,
        val humidity: Float,
        val timestamp: String
    )


    data class HumidityData(
        val id: Int,
        val humidity: Float,
        val timestamp: String
    )


    @GET("api/waterlevel/recent")
 suspend fun getAllWaterLevels(
     @Header("Authorization") authHeader: String
 ): Response<List<WaterLevel>>


    data class GetAllWaterLevelsResponse(
        val status: String,
    val message: String,
         val data: List<WaterLevel>
    )

    data class WaterLevel(
         val id: Int,
        val level: Float,
         val status: String,
    val timestamp: String
    )


    data class WaterLevelRequest(
        val email: String,
        val password: String
    )
    data class WaterLevelResponse(
        val id: Int,
        val level: Double,
        val status: String,
        val timestamp: String
    )
    data class CurrentHumidityResponse(
        val id: Int,
        val humidity: Double,
        val timestamp: String,
    )
    data class CurrentTemperatureResponse(
        val id: Int,

        val temperature: Float
    )


    data class LoginResponse(
        val status: String,
        val message: String,
        val data: UserData
    )


    data class UserData(
        val id: Int,
        val name: String,
        val email: String,
        val role: String
    )


}

data class RegisterResponse(
    val name: String,
    val email: String,
    val token: String,
    val status: String,
    val message: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

