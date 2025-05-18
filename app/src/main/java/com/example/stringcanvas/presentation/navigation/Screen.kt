package com.example.stringcanvas.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object SavedInstructions : Screen("saved_instructions")
    object Settings : Screen("settings")

    // Дополнительные экраны без bottom bar
    object Loading : Screen("loading")

    //    object InstructionVariant : Screen("instruction_variant")
    // Пример: инструкция с двумя аргументами
    object InstructionVariant : Screen("instruction_variant/{id1}/{id2}") {
        // Функция, чтобы удобно сформировать route
        fun createRoute(id1: Long, id2: Long): String {
            return "instruction_variant/$id1/$id2"
        }
    }

    object Instruction : Screen("instruction_screen/{instructionId}") {
        // Функция для удобного создания route с параметром
        fun createRoute(instructionId: Long): String {
            return "instruction_screen/$instructionId"
        }
    }
}