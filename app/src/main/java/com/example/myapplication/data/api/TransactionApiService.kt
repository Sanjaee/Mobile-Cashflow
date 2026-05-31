package com.example.myapplication.data.api

import com.example.myapplication.data.model.TransactionListResponse
import com.example.myapplication.data.model.TransactionRequest
import com.example.myapplication.data.model.TransactionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

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
}
