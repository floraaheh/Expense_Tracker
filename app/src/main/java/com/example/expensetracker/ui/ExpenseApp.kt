//package com.example.expensetracker.ui
//
//import androidx.compose.runtime.Composable
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import com.example.expensetracker.viewmodel.ExpenseViewModel
//@Composable
//fun ExpenseApp() {
//    val navController = rememberNavController()
//    val viewModel: ExpenseViewModel = viewModel()
//
//    NavHost(navController = navController, startDestination = "dashboard") {
//        composable("dashboard") {
//            DashboardScreen(
//                viewModel = viewModel,
//                onNavigateToAdd = { navController.navigate("add_expense") }
//            )
//        }
//        composable("add_expense") {
//            AddExpenseScreen(
//                viewModel = viewModel,
//                onBack = { navController.popBackStack() }
//            )
//        }
//    }
//}
package com.example.expensetracker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.expensetracker.viewmodel.ExpenseViewModel

// Sealed class to define our navigation items for type safety
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.List)
    object Add : Screen("add", "Add", Icons.Default.Add)
}

val items = listOf(
    Screen.Dashboard,
    Screen.Add,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseApp() {
    val navController = rememberNavController()
    val viewModel: ExpenseViewModel = viewModel()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Dashboard.route, Modifier.padding(innerPadding)) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(viewModel)
            }
            composable(Screen.Add.route) {
                AddExpenseScreen(viewModel) {
                    // Navigate back to dashboard after adding an expense
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            }
        }
    }
}
