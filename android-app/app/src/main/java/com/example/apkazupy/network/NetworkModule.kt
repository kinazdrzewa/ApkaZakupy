package com.example.apkazupy.network

import com.example.apkazupy.data.Product
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object NetworkModule {

    // For Android emulator use 10.0.2.2 to reach host machine
    private const val BASE_URL = "http://127.0.0.1:8080/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // Scalars converter first so plain text (e.g. text/plain) responses are handled
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val productApi: ProductApi by lazy {
        retrofit.create(ProductApi::class.java)
    }

    val userApi: UserApi by lazy {
        retrofit.create(UserApi::class.java)
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    val suggestionApi: SuggestionApi by lazy {
        retrofit.create(SuggestionApi::class.java)
    }

}
