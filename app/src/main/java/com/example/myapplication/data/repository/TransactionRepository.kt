package com.example.myapplication.data.repository

import com.example.myapplication.data.api.MonthlyFinanceApiService
import com.example.myapplication.data.api.TransactionApiService
import com.example.myapplication.data.model.MonthlyFinance
import com.example.myapplication.data.model.SetIncomeRequest
import com.example.myapplication.data.model.Transaction
import com.example.myapplication.data.model.TransactionRequest
import com.example.myapplication.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.first

class TransactionRepository(
    private val transactionApiService: TransactionApiService,
    private val monthlyFinanceApiService: MonthlyFinanceApiService,
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
                // Try to parse error message from body
                val errorMsg = response.errorBody()?.string()?.let {
                    try {
                        val json = org.json.JSONObject(it)
                        json.optJSONObject("error")?.optString("message") ?: json.optString("message")
                    } catch (e: Exception) { null }
                } ?: "Failed to create transaction"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMonthlyFinance(year: Int, month: Int): Result<MonthlyFinance> {
        return try {
            val response = monthlyFinanceApiService.getMonthlyFinance(getToken(), year, month)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Failed to fetch monthly finance"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setIncome(month: Int, year: Int, income: Double): Result<MonthlyFinance> {
        return try {
            val response = monthlyFinanceApiService.setIncome(
                getToken(), SetIncomeRequest(month, year, income)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Failed to set income"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTransaction(id: String, request: TransactionRequest): Result<Transaction> {
        return try {
            val response = transactionApiService.updateTransaction(getToken(), id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                val errorMsg = response.errorBody()?.string()?.let {
                    try {
                        val json = org.json.JSONObject(it)
                        json.optJSONObject("error")?.optString("message") ?: json.optString("message")
                    } catch (e: Exception) { null }
                } ?: "Failed to update transaction"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTransaction(id: String): Result<Boolean> {
        return try {
            val response = transactionApiService.deleteTransaction(getToken(), id)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                val errorMsg = response.errorBody()?.string()?.let {
                    try {
                        val json = org.json.JSONObject(it)
                        json.optJSONObject("error")?.optString("message") ?: json.optString("message")
                    } catch (e: Exception) { null }
                } ?: "Failed to delete transaction"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
