package com.example.apkazupy.network

import com.example.apkazupy.data.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.Path

interface UserApi {
    @GET("api/users")
    suspend fun getAll(): List<User>

    @POST("api/users/register")
    suspend fun register(@Body user: User): Response<User>

    @POST("api/users/login")
    suspend fun login(@Body user: User): Response<User>

    @DELETE("api/users/{id}")
    suspend fun delete(@Path("id") id: Long): Response<Void>
}
