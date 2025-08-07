package com.example.expensetracker.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import com.example.expensetracker.data.TransactionDao
import com.example.expensetracker.data.Transaction

class TransactionRepository(private val transactionDao: TransactionDao) {

    private val firestore = Firebase.firestore
    private val userId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    val allTransactions = transactionDao.getAllTransactions()

    suspend fun insert(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
        userId?.let { uid ->
            firestore.collection("users").document(uid).collection("transactions")
                .add(transaction)
                .addOnSuccessListener { Log.d("Firestore", "Transaction added") }
                .addOnFailureListener { e -> Log.w("Firestore", "Error adding document", e) }
        }
    }

    suspend fun delete(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
        userId?.let { uid ->
            if (transaction.firestoreId.isNotBlank()) {
                firestore.collection("users").document(uid).collection("transactions")
                    .document(transaction.firestoreId).delete()
                    .addOnSuccessListener { Log.d("Firestore", "Transaction deleted") }
                    .addOnFailureListener { e -> Log.w("Firestore", "Error deleting document", e) }
            }
        }
    }
}
