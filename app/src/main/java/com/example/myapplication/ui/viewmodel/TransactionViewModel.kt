package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.MonthlyFinance
import com.example.myapplication.data.model.Transaction
import com.example.myapplication.data.model.TransactionRequest
import com.example.myapplication.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class TransactionUiState(
    val transactions: List<Transaction> = emptyList(),
    val monthlyFinance: MonthlyFinance? = null,
    val isLoading: Boolean = false,
    val isMonthlyLoading: Boolean = false,
    val isSettingIncome: Boolean = false,
    val errorMessage: String? = null,
    val addSuccess: Boolean = false,
    val updateSuccess: Boolean = false,
    val deleteSuccess: Boolean = false,
    val incomeSetSuccess: Boolean = false
)

class TransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    init {
        fetchAll()
    }

    fun fetchAll() {
        val now = Calendar.getInstance()
        val month = now.get(Calendar.MONTH) + 1 // 1-12
        val year = now.get(Calendar.YEAR)
        fetchTransactions()
        fetchMonthlyFinance(year, month)
    }

    fun fetchTransactions() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val result = repository.getTransactions()
            result.onSuccess { transactions ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    transactions = transactions
                )
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "An error occurred"
                )
            }
        }
    }

    fun fetchMonthlyFinance(year: Int, month: Int) {
        _uiState.value = _uiState.value.copy(isMonthlyLoading = true)
        viewModelScope.launch {
            val result = repository.getMonthlyFinance(year, month)
            result.onSuccess { monthly ->
                _uiState.value = _uiState.value.copy(
                    isMonthlyLoading = false,
                    monthlyFinance = monthly
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(isMonthlyLoading = false)
            }
        }
    }

    fun setMonthlyIncome(income: Double) {
        val now = Calendar.getInstance()
        val month = now.get(Calendar.MONTH) + 1
        val year = now.get(Calendar.YEAR)
        _uiState.value = _uiState.value.copy(isSettingIncome = true, errorMessage = null, incomeSetSuccess = false)
        viewModelScope.launch {
            val result = repository.setIncome(month, year, income)
            result.onSuccess { monthly ->
                _uiState.value = _uiState.value.copy(
                    isSettingIncome = false,
                    monthlyFinance = monthly,
                    incomeSetSuccess = true
                )
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isSettingIncome = false,
                    errorMessage = exception.message ?: "Failed to set income"
                )
            }
        }
    }

    fun addTransaction(type: String, amount: Double, description: String, date: String?) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, addSuccess = false)
        viewModelScope.launch {
            val request = TransactionRequest(type, amount, description, date)
            val result = repository.createTransaction(request)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    addSuccess = true
                )
                fetchAll()
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Failed to add transaction"
                )
            }
        }
    }

    fun updateTransaction(id: String, type: String, amount: Double, description: String, date: String?) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, updateSuccess = false)
        viewModelScope.launch {
            val request = TransactionRequest(type, amount, description, date)
            val result = repository.updateTransaction(id, request)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    updateSuccess = true
                )
                fetchAll()
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Failed to update transaction"
                )
            }
        }
    }

    fun deleteTransaction(id: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, deleteSuccess = false)
        viewModelScope.launch {
            val result = repository.deleteTransaction(id)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    deleteSuccess = true
                )
                fetchAll()
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Failed to delete transaction"
                )
            }
        }
    }

    fun resetSuccessStates() {
        _uiState.value = _uiState.value.copy(addSuccess = false, updateSuccess = false, deleteSuccess = false)
    }

    fun resetAddSuccess() {
        _uiState.value = _uiState.value.copy(addSuccess = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
