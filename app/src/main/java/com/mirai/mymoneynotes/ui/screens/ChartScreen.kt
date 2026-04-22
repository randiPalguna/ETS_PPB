package com.mirai.mymoneynotes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mirai.mymoneynotes.data.Transaction
import com.mirai.mymoneynotes.data.TransactionType
import com.mirai.mymoneynotes.ui.theme.DarkBrown
import com.mirai.mymoneynotes.ui.theme.Gold
import com.mirai.mymoneynotes.ui.theme.Terracotta
import com.mirai.mymoneynotes.ui.theme.TeaGreen
import com.mirai.mymoneynotes.viewmodel.TransactionViewModel
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.mirai.mymoneynotes.ui.theme.TeaGreenLight
import com.mirai.mymoneynotes.ui.theme.WarmIvoryDark
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(
    viewModel: TransactionViewModel = viewModel()
) {
    val transactions by viewModel.filteredTransactions.collectAsState(initial = emptyList())
    val allTransactions by viewModel.allTransactions.collectAsState(initial = emptyList())
    val selectedMonth by viewModel.selectedMonth.collectAsState(initial = null)
    var selectedTab by remember { mutableStateOf(0) }
    var showDatePicker by remember { mutableStateOf(false) }
    val tabs = listOf("Expense", "Income")

    val calendar = Calendar.getInstance()
    val selectedMonthValue = selectedMonth
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
                return cal.get(Calendar.DAY_OF_MONTH) == 1
            }

            override fun isSelectableYear(year: Int): Boolean {
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                return year in (currentYear - 10)..currentYear
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Analytics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            MonthFilterButton(
                selectedMonth = selectedMonthValue,
                onClick = { showDatePicker = true }
            )
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        val filteredTransactions = transactions.filter {
            if (selectedTab == 0) it.type == TransactionType.EXPENSE
            else it.type == TransactionType.INCOME
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (filteredTransactions.isEmpty()) {
                Box(modifier = Modifier.padding(16.dp)) {
                    EmptyChartState()
                }
            } else {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    CategoryBreakdownCard(
                        transactions = filteredTransactions,
                        type = if (selectedTab == 0) TransactionType.EXPENSE else TransactionType.INCOME
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (allTransactions.isNotEmpty()) {
                        DailyTrendCard(
                            transactions = allTransactions.filter { 
                                if (selectedTab == 0) it.type == TransactionType.EXPENSE 
                                else it.type == TransactionType.INCOME 
                            },
                            selectedMonth = selectedMonth
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    if (showDatePicker) {
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
                        // Set to "All Time" when dismiss is clicked
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

@Composable
fun MonthFilterButton(
    selectedMonth: Pair<Int, Int>?,
    onClick: () -> Unit
) {
    val monthText = if (selectedMonth != null) {
        val year = selectedMonth.first
        val month = selectedMonth.second
        "${getMonthName(month)} $year"
    } else {
        "All Time"
    }

    Button(
        onClick = onClick,
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

@Composable
fun CategoryBreakdownCard(
    transactions: List<Transaction>,
    type: TransactionType
) {
    val categoryTotals = transactions
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .entries
        .sortedByDescending { it.value }

    val totalAmount = categoryTotals.sumOf { it.value }
    
    val palette = if (type == TransactionType.EXPENSE) {
        listOf(
            Color(0xFFE57373), Color(0xFFFFB74D), Color(0xFFFFF176),
            Color(0xFF9575CD), Color(0xFF4FC3F7), Color(0xFFF06292)
        )
    } else {
        listOf(
            Color(0xFF81C784), Color(0xFF4DB6AC), Color(0xFF64B5F6),
            Color(0xFFFFD54F), Color(0xFFBA68C8), Color(0xFFFF8A65)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Category Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            SimplePieChart(
                data = categoryTotals.toList(),
                colors = palette,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            categoryTotals.forEachIndexed { index, entry ->
                CategoryBar(
                    category = entry.key,
                    amount = entry.value,
                    percentage = (entry.value / totalAmount * 100),
                    color = palette[index % palette.size],
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.material3.HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            TotalSummaryRow(
                label = "Total ${if (type == TransactionType.INCOME) "Income" else "Expense"}",
                amount = totalAmount,
                color = if (type == TransactionType.INCOME) TeaGreen else Terracotta
            )
        }
    }
}

@Composable
fun DailyTrendCard(
    transactions: List<Transaction>,
    selectedMonth: Pair<Int, Int>?
) {
    // Determine the month and year to show
    val calendar = java.util.Calendar.getInstance()
    val (targetYear, targetMonth) = if (selectedMonth != null) {
        selectedMonth
    } else {
        Pair(calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH))
    }

    // Aggregate transactions by day for the selected/current month
    val dailyData = remember(transactions, targetYear, targetMonth) {
        val filtered = transactions.filter { tx ->
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = tx.date
            cal.get(java.util.Calendar.YEAR) == targetYear && cal.get(java.util.Calendar.MONTH) == targetMonth
        }

        val daysInMonth = java.util.Calendar.getInstance().apply {
            set(targetYear, targetMonth, 1)
        }.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)

        // Initialize all days with 0.0
        val dataMap = (1..daysInMonth).associateWith { 0.0 }.toMutableMap()

        filtered.forEach { tx ->
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = tx.date
            val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
            dataMap[day] = (dataMap[day] ?: 0.0) + tx.amount
        }
        dataMap.toSortedMap()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Daily Spending (${getMonthName(targetMonth)})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (dailyData.values.all { it == 0.0 }) {
                Text(
                    text = "No data available for this month",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                DailySpendingLineChart(
                    data = dailyData.values.toList(),
                    labels = dailyData.keys.map { it.toString() }
                )
            }
        }
    }
}

@Composable
fun DailySpendingLineChart(
    data: List<Double>,
    labels: List<String>
) {
    val maxValue = (data.maxOrNull() ?: 1.0).coerceAtLeast(1.0).toFloat()
    val minValue = 0f
    val range = maxValue - minValue

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val padding = 20.dp.toPx()
            val chartWidth = canvasWidth - (padding * 2)
            val chartHeight = canvasHeight - (padding * 2)

            // Draw grid lines
            val gridLines = 5
            for (i in 0..gridLines) {
                val y = padding + (chartHeight / gridLines) * i
                drawLine(
                    color = androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.3f),
                    start = androidx.compose.ui.geometry.Offset(padding, y),
                    end = androidx.compose.ui.geometry.Offset(canvasWidth - padding, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            if (data.isNotEmpty()) {
                val points = data.mapIndexed { index, value ->
                    val x = padding + (chartWidth / (data.size - 1)) * index
                    val normalizedValue = ((value.toFloat() - minValue) / range)
                    val y = padding + chartHeight - (normalizedValue * chartHeight)
                    androidx.compose.ui.geometry.Offset(x, y)
                }

                // Draw line
                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = TeaGreen,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // Draw points only for days with spending to avoid clutter
                data.forEachIndexed { index, value ->
                    if (value > 0) {
                        drawCircle(
                            color = TeaGreen,
                            radius = 4.dp.toPx(),
                            center = points[index]
                        )
                        drawCircle(
                            color = androidx.compose.ui.graphics.Color.White,
                            radius = 2.dp.toPx(),
                            center = points[index]
                        )
                    }
                }
            }
        }

        // Draw X-axis labels (showing only some days to avoid crowding)
        if (labels.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val step = 5 // Show label every 5 days
                labels.forEachIndexed { index, label ->
                    if ((index + 1) % step == 0 || index == 0 || index == labels.size - 1) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SimplePieChart(
    data: List<Map.Entry<String, Double>>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.value }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasSize = size.minDimension
            val radius = canvasSize / 2
            val strokeWidth = 40.dp.toPx()

            var startAngle = -90f

            data.forEachIndexed { index, entry ->
                val sweepAngle = (entry.value / total * 360).toFloat()
                val color = colors[index % colors.size]

                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = strokeWidth
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        width = canvasSize,
                        height = canvasSize
                    ),
                    topLeft = Offset(
                        x = (size.width - canvasSize) / 2,
                        y = (size.height - canvasSize) / 2
                    )
                )

                startAngle += sweepAngle
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 40.dp)
        ) {
            Text(
                text = formatCurrency(total),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Total",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun CategoryBar(
    category: String,
    amount: Double,
    percentage: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(color, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(0.4f)
            ) {
                Text(
                    text = formatCurrency(amount),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${String.format("%.1f", percentage)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage.toFloat() / 100f)
                    .height(6.dp)
                    .background(color, RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
fun TotalSummaryRow(
    label: String,
    amount: Double,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = formatCurrency(amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun EmptyChartState() {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
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
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No data to display",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add transactions to see analytics",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return "Rp ${formatter.format(amount)}"
}

private fun getMonthName(month: Int): String {
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    return months.getOrElse(month) { "Unknown" }
}
