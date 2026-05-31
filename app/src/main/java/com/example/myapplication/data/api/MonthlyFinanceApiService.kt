package com.example.myapplication.data.api

import com.example.myapplication.data.model.MonthlyFinanceResponse
import com.example.myapplication.data.model.SetIncomeRequest
import com.example.myapplication.data.model.SetIncomeResponse
import retrofit2.Response
import retrofit2.http.*

interface MonthlyFinanceApiService {

    @POST("api/v1/monthly-finance/set-income")
    suspend fun setIncome(
        @Header("Authorization") token: String,
        @Body request: SetIncomeRequest
    ): Response<SetIncomeResponse>

    @GET("api/v1/monthly-finance/{year}/{month}")
    suspend fun getMonthlyFinance(
        @Header("Authorization") token: String,
        @Path("year") year: Int,
        @Path("month") month: Int
    ): Response<MonthlyFinanceResponse>
}
