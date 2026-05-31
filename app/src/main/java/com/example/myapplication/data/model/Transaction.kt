package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class Transaction(
    val id: String,
    val type: String,
    val amount: Double,
    val description: String,
    val date: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("user_id")
    val userId: String
)

data class TransactionListResponse(
    val data: List<Transaction>
)

data class TransactionRequest(
    val type: String,
    val amount: Double,
    val description: String,
    val date: String? = null
)

data class TransactionResponse(
    val message: String,
    val data: Transaction
)
