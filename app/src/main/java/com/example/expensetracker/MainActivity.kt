package com.example.expensetracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.expensetracker.ui.ExpenseApp
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Attempt to sign in to Firebase anonymously
        Firebase.auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in success, log the user ID.
                    Log.d("Firebase", "signInAnonymously:success, user=${Firebase.auth.currentUser?.uid}")
                } else {
                    // If sign in fails, log a warning.
                    Log.w("Firebase", "signInAnonymously:failure", task.exception)
                }
            }

        setContent {
            ExpenseTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // This is the line that runs your entire UI.
                    ExpenseApp()
                }
            }
        }
    }
}