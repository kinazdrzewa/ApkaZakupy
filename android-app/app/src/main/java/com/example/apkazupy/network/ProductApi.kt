package com.example.apkazupy.network

import com.example.apkazupy.data.Product
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ProductApi {
    @GET("api/products")
    suspend fun getAll(): List<Product>

    @POST("api/products")
    suspend fun add(@Body product: Product): Product
}
