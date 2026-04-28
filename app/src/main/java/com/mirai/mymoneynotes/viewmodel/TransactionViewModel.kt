package com.mirai.mymoneynotes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mirai.mymoneynotes.data.Transaction
import com.mirai.mymoneynotes.data.TransactionRepository
import com.mirai.mymoneynotes.data.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TransactionRepository.getInstance(application)

    val allTransactions = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SHARING_TIMEOUT_MS), emptyList())

    val totalIncome = repository.getTotalIncome()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SHARING_TIMEOUT_MS), 0.0)

    val totalExpense = repository.getTotalExpense()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SHARING_TIMEOUT_MS), 0.0)

    val balance = repository.getBalance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SHARING_TIMEOUT_MS), 0.0)

    val transactionCount = repository.getTransactionCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SHARING_TIMEOUT_MS), 0)

    private val _selectedTypeFilter = MutableStateFlow<TransactionType?>(null)
    val selectedTypeFilter = _selectedTypeFilter.asStateFlow()

    // Month filtering: Pair<Year, Month> or null for all time
    private val _selectedMonth = MutableStateFlow<Pair<Int, Int>?>(null)
    val selectedMonth = _selectedMonth.asStateFlow()

    val filteredTransactions = combine(
        allTransactions,
        _selectedTypeFilter,
        _selectedMonth
    ) { transactions, typeFilter, monthFilter ->
        applyFilters(
            transactions = transactions,
            typeFilter = typeFilter,
            monthFilter = monthFilter
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SHARING_TIMEOUT_MS), emptyList())

    private fun applyFilters(
        transactions: List<Transaction>,
        typeFilter: TransactionType?,
        monthFilter: Pair<Int, Int>?
    ): List<Transaction> {
        if (typeFilter == null && monthFilter == null) {
            return transactions
        }

        val calendar = Calendar.getInstance()
        return transactions.filter { transaction ->
            val matchesType = typeFilter == null || transaction.type == typeFilter
            if (!matchesType) return@filter false

            if (monthFilter == null) return@filter true
            val (year, month) = monthFilter
            calendar.timeInMillis = transaction.date
            calendar.get(Calendar.YEAR) == year &&
                calendar.get(Calendar.MONTH) == month
        }
    }

    fun addTransaction(
        type: TransactionType,
        category: String,
        amount: String,
        description: String,
        date: Long
    ) {
        viewModelScope.launch {
            val amountValue = amount.trim().toDoubleOrNull() ?: 0.0
            if (amountValue > 0 && category.isNotBlank()) {
                val transaction = Transaction(
                    type = type,
                    category = category,
                    amount = amountValue,
                    date = date,
                    description = description
                )
                repository.insertTransaction(transaction)
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun filterByType(type: TransactionType?) {
        _selectedTypeFilter.value = type
    }

    fun filterByMonth(year: Int, month: Int?) {
        _selectedMonth.value = month?.let { year to it }
    }

    fun getIncomeCategories() = INCOME_CATEGORIES
    fun getExpenseCategories() = EXPENSE_CATEGORIES

    fun getAvailableMonths(): List<Pair<Int, Int>> {
        val transactions = allTransactions.value
        val calendar = Calendar.getInstance()
        val monthsSet = mutableSetOf<Pair<Int, Int>>()

        transactions.forEach { transaction ->
            calendar.timeInMillis = transaction.date
            monthsSet.add(Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)))
        }

        return monthsSet.sortedByDescending { it.first * 100 + it.second }
    }

    companion object {
        private const val SHARING_TIMEOUT_MS = 5_000L
        private val INCOME_CATEGORIES = listOf("Salary", "Bonus", "Gift", "Investment", "Other")
        private val EXPENSE_CATEGORIES = listOf(
            "Food",
            "Transport",
            "Utilities",
            "Entertainment",
            "Shopping",
            "Health",
            "Other"
        )
    }
}
