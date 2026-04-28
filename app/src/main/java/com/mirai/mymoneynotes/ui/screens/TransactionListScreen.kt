package com.mirai.mymoneynotes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mirai.mymoneynotes.data.TransactionType
import com.mirai.mymoneynotes.ui.components.TransactionItem
import com.mirai.mymoneynotes.viewmodel.TransactionViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    viewModel: TransactionViewModel = viewModel()
) {
    val transactions by viewModel.filteredTransactions.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedTypeFilter by viewModel.selectedTypeFilter.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    val selectedMonthValue = selectedMonth

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Transaction History",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        FilterChips(
            selectedFilter = selectedTypeFilter,
            selectedMonth = selectedMonthValue,
            onFilterSelected = viewModel::filterByType,
            onMonthFilterClick = { showDatePicker = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (transactions.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onDelete = { viewModel.deleteTransaction(transaction) }
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        val initialMonth = if (selectedMonthValue != null) {
            calendar.set(selectedMonthValue.first, selectedMonthValue.second, 1)
            calendar.timeInMillis
        } else {
            System.currentTimeMillis()
        }

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMonth,
            initialDisplayedMonthMillis = initialMonth,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = utcTimeMillis
                    // Only allow selecting the 1st day of each month for month filtering
                    return cal.get(Calendar.DAY_OF_MONTH) == 1
                }

                override fun isSelectableYear(year: Int): Boolean {
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    return year in (currentYear - 5)..currentYear
                }
            }
        )

        val confirmEnabled = derivedStateOf {
            datePickerState.selectedDateMillis != null
        }

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate ->
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = selectedDate
                            viewModel.filterByMonth(
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH)
                            )
                        }
                        showDatePicker = false
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        // Set to "All Time" when cancel is clicked
                        viewModel.filterByMonth(0, null)
                        showDatePicker = false
                    }
                ) {
                    Text("All Time")
                }
            }
        ) {
            androidx.compose.material3.DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = "Select Month",
                        modifier = Modifier.padding(16.dp)
                    )
                },
                headline = {
                    Text(
                        text = "Choose the 1st day of the month",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChips(
    selectedFilter: TransactionType?,
    selectedMonth: Pair<Int, Int>?,
    onFilterSelected: (TransactionType?) -> Unit,
    onMonthFilterClick: () -> Unit
) {
    val monthText = if (selectedMonth != null) {
        val year = selectedMonth.first
        val month = selectedMonth.second
        "${getMonthName(month)} $year"
    } else {
        "All Time"
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { onFilterSelected(null) },
                label = { Text("All") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selectedFilter == TransactionType.INCOME,
                onClick = { onFilterSelected(TransactionType.INCOME) },
                label = { Text("Income") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selectedFilter == TransactionType.EXPENSE,
                onClick = { onFilterSelected(TransactionType.EXPENSE) },
                label = { Text("Expense") },
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = onMonthFilterClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedMonth != null) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
            elevation = if (selectedMonth != null) {
                ButtonDefaults.buttonElevation(4.dp)
            } else {
                ButtonDefaults.buttonElevation(0.dp)
            }
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Filter by month",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(text = monthText)
        }
    }
}

@Composable
fun EmptyState() {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No transactions yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add your first transaction to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

private fun getMonthName(month: Int): String {
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    return months.getOrElse(month) { "Unknown" }
}
