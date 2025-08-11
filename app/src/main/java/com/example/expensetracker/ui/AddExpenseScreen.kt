
package com.example.expensetracker.ui
import androidx.compose.ui.graphics.Color
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.Transaction
import com.example.expensetracker.viewmodel.CATEGORIES
import com.example.expensetracker.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch
import java.util.Date
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(viewModel: ExpenseViewModel, onBack: () -> Unit) {
    var rawInput by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(CATEGORIES[0]) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var isCategoryMenuExpanded by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    fun handleAddTransaction() {
        val amountValue = amount.toDoubleOrNull()
        if (description.isNotBlank() && amountValue != null) {
            viewModel.insert(
                Transaction(
                    description = description,
                    amount = amountValue,
                    category = category,
                    date = Date()
                )
            )
            onBack()
        } else {
            error = "Please ensure all fields are valid."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Expense") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("AI Helper", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = rawInput,
                onValueChange = { rawInput = it },
                label = { Text("Describe expense (e.g., Dinner for $50)") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    isAnalyzing = true
                    coroutineScope.launch {
                        viewModel.analyzeExpense(rawInput).onSuccess { analysis ->
                            description = analysis.description
                            amount = analysis.amount.toString()
                            if (CATEGORIES.contains(analysis.category)) {
                                category = analysis.category
                            }
                            error = null
                        }.onFailure { e ->
                            error = "Could not analyze. Please fill manually."
                        }
                        isAnalyzing = false
                    }
                },
                enabled = !isAnalyzing,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("✨ Analyze with AI")
                }
            }

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Manual Entry", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth()
            )

            // Proper Dropdown Menu for Category
            ExposedDropdownMenuBox(
                expanded = isCategoryMenuExpanded,
                onExpandedChange = { isCategoryMenuExpanded = !isCategoryMenuExpanded }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = isCategoryMenuExpanded,
                    onDismissRequest = { isCategoryMenuExpanded = false }
                ) {
                    CATEGORIES.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                category = selectionOption
                                isCategoryMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f)) // Pushes the button to the bottom

            Button(
                onClick = ::handleAddTransaction,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Save Transaction", fontSize = 16.sp)
            }
        }
    }
}
