package com.example.myapplication.data.api

import com.example.myapplication.data.model.TransactionListResponse
import com.example.myapplication.data.model.TransactionRequest
import com.example.myapplication.data.model.TransactionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface TransactionApiService {
    @GET("api/v1/transactions")
    suspend fun getTransactions(
        @Header("Authorization") token: String
    ): Response<TransactionListResponse>

    @POST("api/v1/transactions")
    suspend fun createTransaction(
        @Header("Authorization") token: String,
        @Body request: TransactionRequest
    ): Response<TransactionResponse>

    @PUT("api/v1/transactions/{id}")
    suspend fun updateTransaction(
        @Header("Authorization") token: String,
        @retrofit2.http.Path("id") id: String,
        @Body request: TransactionRequest
    ): Response<TransactionResponse>

    @retrofit2.http.DELETE("api/v1/transactions/{id}")
    suspend fun deleteTransaction(
        @Header("Authorization") token: String,
        @retrofit2.http.Path("id") id: String
    ): Response<Any>
}
