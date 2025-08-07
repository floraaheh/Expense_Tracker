package com.example.expensetracker.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.expensetracker.BuildConfig
import com.example.expensetracker.data.AppDatabase
import com.example.expensetracker.data.Transaction
import com.example.expensetracker.repository.TransactionRepository
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Date

val CATEGORIES = listOf("Food", "Transport", "Shopping", "Utilities", "Health", "Entertainment", "Other")

@Serializable
data class ExpenseAnalysis(
    val description: String,
    val category: String,
    val amount: Double
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository
    val allTransactions: LiveData<List<Transaction>>

    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    init {
        val transactionDao = AppDatabase.getDatabase(application).transactionDao()
        repository = TransactionRepository(transactionDao)
        allTransactions = repository.allTransactions.asLiveData()
    }

    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
    }

    fun delete(transaction: Transaction) = viewModelScope.launch {
        repository.delete(transaction)
    }

    suspend fun analyzeExpense(prompt: String): Result<ExpenseAnalysis> {
        return try {
            val fullPrompt = """
                Analyze the following expense entry and extract the description, category, and amount.
                Entry: "$prompt"
                Respond ONLY with a valid JSON object containing "description" (string), "category" (string), and "amount" (number).
                The category must be one of the following: ${CATEGORIES.joinToString(", ")}.
                Example: {"description": "Coffee with friend", "category": "Food", "amount": 5.75}
            """.trimIndent()

            val response = generativeModel.generateContent(fullPrompt)

            val responseText = response.text
                ?: return Result.failure(Exception("API returned an empty response."))

            val jsonString = responseText.substringAfter('{', "").substringBeforeLast('}', "")
            if (jsonString.isBlank()) {
                return Result.failure(Exception("Could not find JSON in the API response."))
            }
            val fullJsonString = "{$jsonString}"

            val analysis = Json { ignoreUnknownKeys = true }.decodeFromString<ExpenseAnalysis>(fullJsonString)
            Result.success(analysis)
        } catch (e: Exception) {
            Log.e("Gemini", "Error analyzing expense or parsing JSON: ${e.message}")
            Result.failure(e)
        }
    }
}