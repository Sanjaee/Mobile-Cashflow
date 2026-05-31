package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Transaction
import com.example.myapplication.data.model.TransactionRequest
import com.example.myapplication.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TransactionUiState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val addSuccess: Boolean = false
)

class TransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    init {
        fetchTransactions()
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
                // Refresh list
                fetchTransactions()
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Failed to add transaction"
                )
            }
        }
    }

    fun resetAddSuccess() {
        _uiState.value = _uiState.value.copy(addSuccess = false)
    }
}
