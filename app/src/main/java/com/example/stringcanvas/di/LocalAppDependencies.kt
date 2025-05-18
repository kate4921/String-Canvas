package com.example.stringcanvas.di

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.stringcanvas.data.database.InstructionDao

val LocalInstructionDao = staticCompositionLocalOf<InstructionDao> {
    error("InstructionDao not provided. Did you forget to call DatabaseProvider.init()?")
}