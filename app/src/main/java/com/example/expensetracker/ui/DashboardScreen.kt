package com.example.expensetracker.ui
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartData
import com.example.expensetracker.data.Transaction
import com.example.expensetracker.viewmodel.CATEGORIES
import com.example.expensetracker.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

// A predefined color palette for consistency in the chart.
private val categoryColors = listOf(
    Color(0xFF4A90E2), Color(0xFF50E3C2), Color(0xFFB8E986), Color(0xFFF8E71C),
    Color(0xFFF5A623), Color(0xFFBD10E0), Color(0xFF9013FE)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: ExpenseViewModel) {
    val transactions by viewModel.allTransactions.observeAsState(initial = emptyList())
    val totalExpenseThisMonth = transactions.sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            item { SummaryCard(totalExpenseThisMonth) }
            item {
                if (transactions.isNotEmpty()) {
                    SpendingChart(transactions, totalExpenseThisMonth)
                }
            }
            item {
                Text(
                    "Recent Transactions",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No transactions yet. Add one!", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                items(transactions) { transaction ->
                    TransactionItem(transaction)
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun SummaryCard(totalExpense: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Total Expenses This Month", style = MaterialTheme.typography.labelMedium)
            Text(
                String.format("$%.2f", totalExpense),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SpendingChart(transactions: List<Transaction>, totalExpense: Double) {
    val categoryTotals = transactions.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    val pieChartData = PieChartData(
        slices = categoryTotals.map { (category, total) ->
            PieChartData.Slice(
                label = category,
                value = total.toFloat(),
                color = getCategoryColor(category)
            )
        },
        plotType = PlotType.Donut
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Spending by Category", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            if (pieChartData.slices.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f).height(150.dp)) {
                        PieChart(
                            modifier = Modifier.fillMaxSize(),
                            pieChartData = pieChartData,
                            pieChartConfig = co.yml.charts.ui.piechart.models.PieChartConfig(
                                sliceLabelTextSize = 12.sp,
                                isAnimationEnable = true,
                                showSliceLabels = false, // Labels are now in the legend
                                strokeWidth = 100f
                            )
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    // Display the custom legend
                    ChartLegend(categoryTotals, totalExpense)
                }
            }
        }
    }
}

@Composable
fun ChartLegend(categoryTotals: Map<String, Double>, totalExpense: Double) {
    Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
        categoryTotals.entries.sortedByDescending { it.value }.forEach { (category, total) ->
            if (total > 0) {
                val percentage = (total / totalExpense * 100).toInt()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(getCategoryColor(category))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "$category ($percentage%)",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}


@Composable
fun TransactionItem(transaction: Transaction) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getCategoryIcon(transaction.category),
                contentDescription = transaction.category,
                modifier = Modifier.size(40.dp),
                tint = getCategoryColor(transaction.category) // Use consistent color
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.description, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(transaction.category, style = MaterialTheme.typography.bodyMedium)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    String.format("-$%.2f", transaction.amount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(dateFormat.format(transaction.date), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Food" -> Icons.Default.Fastfood
        "Transport" -> Icons.Default.DirectionsCar
        "Shopping" -> Icons.Default.ShoppingCart
        "Utilities" -> Icons.Default.Home
        "Health" -> Icons.Default.Favorite
        "Entertainment" -> Icons.Default.Movie
        else -> Icons.Default.AttachMoney
    }
}

// Helper function to get a consistent color for each category.
fun getCategoryColor(category: String): Color {
    val index = CATEGORIES.indexOf(category)
    return if (index != -1) {
        categoryColors[index % categoryColors.size]
    } else {
        Color.Gray
    }
}
