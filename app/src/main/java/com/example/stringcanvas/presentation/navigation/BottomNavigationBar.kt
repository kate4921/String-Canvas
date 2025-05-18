package com.example.stringcanvas.presentation.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState


@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        NavBarItem(
            route = Screen.SavedInstructions.route,
            icon = Icons.Default.Person,
            contentDescription = "Saved"
        ),
        NavBarItem(
            route = Screen.Home.route,
            icon = Icons.Default.Add,
            contentDescription = "Create"
        ),
        NavBarItem(
            route = Screen.Settings.route,
            icon = Icons.Default.Settings,
            contentDescription = "Settings"
        )
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp, // Добавляем небольшую тень для разделения
//        modifier = Modifier.height(60.dp) // Делаем панель уже по высоте
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            val selected = currentRoute == item.route
            val iconSize by animateDpAsState(
                targetValue = if (selected) 26.dp else 22.dp,
                animationSpec = tween(durationMillis = 150)
            )

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.contentDescription,
                        modifier = Modifier.size(iconSize)
                    )
                },
                label = null,
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                )
            )
        }
    }
}

//@Composable
//fun BottomNavigationBar(navController: NavHostController) {
//    // Определяем список пунктов нижней панели
//    val items = listOf(
//        NavBarItem(
//            route = Screen.SavedInstructions.route,
//            icon = Icons.Default.Person, // пример иконки для Saved
//            contentDescription = "Saved"
//        ),
//        NavBarItem(
//            route = Screen.Home.route,
//            icon = Icons.Default.Add, // иконка для Home (например, "+")
//            contentDescription = "Create"
//        )
////        ,
////        NavBarItem(
////            route = Screen.Settings.route,
////            icon = Icons.Default.Settings,
////            contentDescription = "Settings"
////        )
//    )
//
//    NavigationBar(
//        containerColor = MaterialTheme.colorScheme.primaryContainer,
//        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
//    ) {
//        val navBackStackEntry by navController.currentBackStackEntryAsState()
//        val currentRoute = navBackStackEntry?.destination?.route
//
//        items.forEach { item ->
//            val selected = currentRoute == item.route
//
//            // Анимация размера иконки: выбранная иконка немного больше
//            val iconSize by animateDpAsState(
//                targetValue = if (selected) 30.dp else 24.dp,
//                animationSpec = tween(durationMillis = 200)
//            )
//
//            NavigationBarItem(
//                selected = selected,
//                onClick = {
//                    navController.navigate(item.route) {
//                        launchSingleTop = true
//                        restoreState = true
//                        popUpTo(navController.graph.findStartDestination().id) {
//                            saveState = true
//                        }
//                    }
//                },
//                icon = {
//                    Icon(
//                        imageVector = item.icon,
//                        contentDescription = item.contentDescription,
//                        modifier = Modifier.size(iconSize)
//                    )
//                },
//                label = null,
//                alwaysShowLabel = false,
//                colors = NavigationBarItemDefaults.colors(
//                    selectedIconColor = MaterialTheme.colorScheme.primary,
//                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
//                    selectedTextColor = MaterialTheme.colorScheme.primary,
//                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
//                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer
//                )
//            )
//        }
//    }
//}
