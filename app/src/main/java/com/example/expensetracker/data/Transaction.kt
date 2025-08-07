package com.example.expensetracker.data



import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import java.util.Date

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @DocumentId
    val firestoreId: String = "",
    val description: String,
    val amount: Double,
    val category: String,
    val date: Date
) {
    // Add a no-argument constructor for Firestore deserialization
    constructor() : this(0, "", "", 0.0, "", Date())
}