package com.example.myapplication.data.model

data class MonthlyFinance(
    val id: String? = null,
    val month: Int,
    val year: Int,
    val total_income: Double,
    val total_expense: Double,
    val balance: Double,
    val user_id: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

data class MonthlyFinanceResponse(
    val data: MonthlyFinance
)

data class SetIncomeRequest(
    val month: Int,
    val year: Int,
    val income: Double
)

data class SetIncomeResponse(
    val message: String,
    val data: MonthlyFinance
)
