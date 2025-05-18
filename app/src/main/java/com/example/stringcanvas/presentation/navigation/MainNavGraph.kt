package com.example.stringcanvas.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.stringcanvas.presentation.screens.home.HomeScreen
import com.example.stringcanvas.presentation.screens.instruction.InstructionScreen
import com.example.stringcanvas.presentation.screens.instructionVariant.InstructionVariantScreen
import com.example.stringcanvas.presentation.screens.LoadingScreen
import com.example.stringcanvas.presentation.screens.savedInstructions.SavedInstructionsScreen
import com.example.stringcanvas.presentation.screens.settings.SettingsScreen
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.navArgument


@Composable
fun MainNavGraph(navController: NavHostController) {
    // Определяем NavHost с начальным маршрутом (например, Home)
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(onNavigateTo = { routeString ->
                navController.navigate(routeString)
            })
        }

        composable(Screen.SavedInstructions.route) {
            SavedInstructionsScreen (
                onNavigateToInstruction = { instructionId ->
                    // Переход на экран InstructionScreen
                    navController.navigate(Screen.Instruction.createRoute(instructionId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }

        composable(Screen.Loading.route) {
            LoadingScreen()
        }

        composable(
            route = Screen.InstructionVariant.route,
            arguments = listOf(
                navArgument("id1") { type = NavType.LongType },
                navArgument("id2") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val id1 = backStackEntry.arguments?.getLong("id1") ?: 0L
            val id2 = backStackEntry.arguments?.getLong("id2") ?: 0L

            InstructionVariantScreen(
                id1 = id1,
                id2 = id2,
                onNavigateBack = {
                    // 1) Переход на Home
                    navController.navigate(Screen.Home.route) {
                        // 2) Попутно очистить бэктрек, чтобы Home не дублировался
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onNavigateToInstruction = { instructionId ->
                    // Переход на экран InstructionScreen
                    navController.navigate(Screen.Instruction.createRoute(instructionId))
                }
            )
        }

        composable(
            route = Screen.Instruction.route,
            arguments = listOf(navArgument("instructionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val instructionId = backStackEntry.arguments?.getLong("instructionId") ?: 0L
            InstructionScreen(
                instructionId = instructionId,
                onNavigateBack = {
                    navController.popBackStack() // Возвращаемся на предыдущий экран
                }
            )
        }
    }
}

// Вспомогательная функция для получения текущего маршрута
@Composable
fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}
