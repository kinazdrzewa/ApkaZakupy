package com.example.apkazupy.network

import com.example.apkazupy.data.Suggestion
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SuggestionApi {
    @GET("api/suggestions")
    suspend fun getAll(@Query("userId") userId: Long? = null): List<Suggestion>

    @POST("api/suggestions")
    suspend fun create(@Body suggestion: Suggestion): Response<Suggestion>

    @DELETE("api/suggestions/{id}")
    suspend fun delete(@Path("id") id: Long): Response<Void>
}