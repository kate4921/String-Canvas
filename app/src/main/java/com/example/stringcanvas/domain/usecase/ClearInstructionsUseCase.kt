package com.example.stringcanvas.domain.usecase

import com.example.stringcanvas.data.cache.TempInstructionsCache

class ClearInstructionsUseCase {
    fun execute(ids: List<Long>) {
        // Просто очищаем
        TempInstructionsCache.clear(*ids.toLongArray())
    }
}

