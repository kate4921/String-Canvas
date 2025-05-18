package com.example.stringcanvas.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.stringcanvas.presentation.ui.RequestNotificationPermission
import com.example.stringcanvas.presentation.navigation.BottomNavigationBar
import com.example.stringcanvas.presentation.navigation.MainNavGraph
import com.example.stringcanvas.presentation.navigation.Screen
import com.example.stringcanvas.presentation.navigation.currentRoute


@Composable
fun MainScreen(navController: NavHostController) {

    // Запрос разрешения на уведомления
    RequestNotificationPermission()

//    val navController = rememberNavController()
    val currentRoute = currentRoute(navController)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentRoute in listOf(
                    Screen.Home.route,
                    Screen.SavedInstructions.route,
                    Screen.Settings.route
                )
            ) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
//            MainNavGraph(navController = navController)
            MainNavGraph(navController)       // тот же nav
        }
    }
}
