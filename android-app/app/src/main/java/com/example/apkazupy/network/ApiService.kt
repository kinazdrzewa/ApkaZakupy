package com.example.apkazupy.network

import com.example.apkazupy.data.Product
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Query

data class CreateListRequest(val name: String, val userId: Long?)
data class ShoppingListDto(val id: Long, val name: String)
data class AddItemRequest(val productId: Long, val quantity: Int)
data class ListItemDto(
    val itemId: Long?,
    val productId: Long?,
    val name: String?,
    val barcode: String?,
    val calories: Double?,
    val protein: Double?,
    val fat: Double?,
    val carbohydrates: Double?,
    val quantity: Int?
)

interface ApiService {
    // existing simple lists API (returns names)
    @GET("/api/lists")
    suspend fun getLists(@Query("userId") userId: Long? = null): List<String>

    @POST("/api/lists")
    suspend fun createList(@Body req: CreateListRequest): Response<String>

    // richer endpoints for client: list details and items
    @GET("/api/lists/details")
    suspend fun getListDetails(@Query("userId") userId: Long? = null): List<ShoppingListDto>

    @DELETE("/api/lists/{id}")
    suspend fun deleteList(@Path("id") listId: Long): Response<Void>

    @GET("/api/lists/{id}/items")
    suspend fun getListItems(@Path("id") listId: Long): List<ListItemDto>

    @POST("/api/lists/{id}/items")
    suspend fun addItemToList(@Path("id") listId: Long, @Body req: AddItemRequest): Response<String>

    data class UpdateQuantityRequest(val quantity: Int)

    @PATCH("/api/lists/{id}/items/{itemId}")
    suspend fun updateItemQuantity(@Path("id") listId: Long, @Path("itemId") itemId: Long, @Body req: UpdateQuantityRequest): Response<ListItemDto>

    @DELETE("/api/lists/{id}/items/{itemId}")
    suspend fun deleteItem(@Path("id") listId: Long, @Path("itemId") itemId: Long): Response<Void>
}
