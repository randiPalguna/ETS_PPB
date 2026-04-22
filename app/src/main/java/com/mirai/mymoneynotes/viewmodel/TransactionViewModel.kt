package com.mirai.mymoneynotes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mirai.mymoneynotes.data.Transaction
import com.mirai.mymoneynotes.data.TransactionRepository
import com.mirai.mymoneynotes.data.TransactionType
import kotlinx.coroutines.flow.Flow
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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalIncome = repository.getTotalIncome()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense = repository.getTotalExpense()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val balance = repository.getBalance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _selectedType = MutableStateFlow(TransactionType.INCOME)
    val selectedType = _selectedType.asStateFlow()

    private val _selectedCategory = MutableStateFlow("")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _amount = MutableStateFlow("")
    val amount = _amount.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _date = MutableStateFlow(System.currentTimeMillis())
    val date = _date.asStateFlow()

    private val _filteredTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val filteredTransactions = _filteredTransactions.asStateFlow()

    // Month filtering: Pair<Year, Month> or null for all time
    private val _selectedMonth = MutableStateFlow<Pair<Int, Int>?>(null)
    val selectedMonth = _selectedMonth.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                allTransactions,
                _selectedMonth
            ) { transactions, month ->
                applyFilters(transactions, month)
            }.collect { filtered ->
                _filteredTransactions.value = filtered
            }
        }
    }

    private fun applyFilters(
        transactions: List<Transaction>,
        month: Pair<Int, Int>?
    ): List<Transaction> {
        return if (month == null) {
            transactions
        } else {
            val (year, monthOfYear) = month
            val calendar = Calendar.getInstance()

            transactions.filter { transaction ->
                calendar.timeInMillis = transaction.date
                calendar.get(Calendar.YEAR) == year &&
                calendar.get(Calendar.MONTH) == monthOfYear
            }
        }
    }

    fun setSelectedType(type: TransactionType) {
        _selectedType.value = type
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setAmount(value: String) {
        _amount.value = value
    }

    fun setDescription(value: String) {
        _description.value = value
    }

    fun setDate(timestamp: Long) {
        _date.value = timestamp
    }

    fun addTransaction(
        type: TransactionType,
        category: String,
        amount: String,
        description: String,
        date: Long
    ) {
        viewModelScope.launch {
            val amountValue = amount.toDoubleOrNull() ?: 0.0
            if (amountValue > 0 && category.isNotBlank()) {
                val transaction = Transaction(
                    type = type,
                    category = category,
                    amount = amountValue,
                    date = date,
                    description = description
                )
                repository.insertTransaction(transaction)
                clearForm()
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun filterByType(type: TransactionType?) {
        viewModelScope.launch {
            val currentMonth = _selectedMonth.value

            if (type == null) {
                allTransactions.collect { transactions ->
                    _filteredTransactions.value = applyFilters(transactions, currentMonth)
                }
            } else {
                repository.getTransactionsByType(type.name).collect { transactions ->
                    _filteredTransactions.value = applyFilters(transactions, currentMonth)
                }
            }
        }
    }

    fun filterByMonth(year: Int, month: Int?) {
        _selectedMonth.value = if (month != null) {
            Pair(year, month)
        } else {
            null
        }

        // Re-apply current type filter with new month filter
        viewModelScope.launch {
            allTransactions.collect { transactions ->
                val filtered = applyFilters(transactions, _selectedMonth.value)
                _filteredTransactions.value = filtered
            }
        }
    }

    fun clearForm() {
        _selectedType.value = TransactionType.INCOME
        _selectedCategory.value = ""
        _amount.value = ""
        _description.value = ""
        _date.value = System.currentTimeMillis()
    }

    fun getIncomeCategories() = listOf("Salary", "Bonus", "Gift", "Investment", "Other")
    fun getExpenseCategories() = listOf("Food", "Transport", "Utilities", "Entertainment", "Shopping", "Health", "Other")

    fun getAvailableMonths(): List<Pair<Int, Int>> {
        val transactions = _filteredTransactions.value
        val calendar = Calendar.getInstance()
        val monthsSet = mutableSetOf<Pair<Int, Int>>()

        transactions.forEach { transaction ->
            calendar.timeInMillis = transaction.date
            monthsSet.add(Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)))
        }

        return monthsSet.sortedByDescending { it.first * 100 + it.second }
    }
}
