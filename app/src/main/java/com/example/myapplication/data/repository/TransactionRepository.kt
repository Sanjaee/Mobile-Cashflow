package com.example.myapplication.data.repository

import com.example.myapplication.data.api.TransactionApiService
import com.example.myapplication.data.model.Transaction
import com.example.myapplication.data.model.TransactionRequest
import com.example.myapplication.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.first

class TransactionRepository(
    private val transactionApiService: TransactionApiService,
    private val preferencesManager: PreferencesManager
) {
    private suspend fun getToken(): String {
        val token = preferencesManager.accessToken.first()
        return "Bearer $token"
    }

    suspend fun getTransactions(): Result<List<Transaction>> {
        return try {
            val response = transactionApiService.getTransactions(getToken())
            if (response.isSuccessful) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch transactions"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTransaction(request: TransactionRequest): Result<Transaction> {
        return try {
            val response = transactionApiService.createTransaction(getToken(), request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Failed to create transaction"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
