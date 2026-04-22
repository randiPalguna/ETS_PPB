package com.mirai.mymoneynotes.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mirai.mymoneynotes.ui.screens.AddTransactionScreen
import com.mirai.mymoneynotes.ui.screens.ChartScreen
import com.mirai.mymoneynotes.ui.screens.HomeScreen
import com.mirai.mymoneynotes.ui.screens.TransactionListScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Add : Screen("add", "Add", Icons.Default.Add)
    object History : Screen("history", "History", Icons.Default.List)
    object Charts : Screen("charts", "Charts", Icons.Default.Info)
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Add, Screen.History, Screen.Charts)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
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
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Add.route) { AddTransactionScreen() }
            composable(Screen.History.route) { TransactionListScreen() }
            composable(Screen.Charts.route) { ChartScreen() }
        }
    }
}
