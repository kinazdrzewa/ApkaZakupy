package com.example.apkazupy.network

import com.example.apkazupy.data.Product
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.PUT
import retrofit2.Response
import retrofit2.http.Path

interface ProductApi {
    @GET("api/products")
    suspend fun getAll(): List<Product>

    @POST("api/products")
    suspend fun add(@Body product: Product): Product

    @PUT("api/products/{id}")
    suspend fun update(@Path("id") id: Long, @Body product: Product): Product

    @DELETE("api/products/{id}")
    suspend fun delete(@Path("id") id: Long): Response<Void>
}
