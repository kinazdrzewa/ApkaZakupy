package com.example.apkazupy.network

import com.example.apkazupy.data.Suggestion
import retrofit2.Response
import com.example.apkazupy.data.Product

suspend fun getSuggestions(): List<Suggestion> =
    NetworkModule.suggestionApi.getAll()

suspend fun deleteSuggestion(id: Long): Response<Void> =
    NetworkModule.suggestionApi.delete(id)

suspend fun createSuggestion(suggestion: Suggestion): Response<Suggestion> =
    NetworkModule.suggestionApi.create(suggestion)

suspend fun createProduct(product: Product): Product =
    NetworkModule.productApi.add(product)

