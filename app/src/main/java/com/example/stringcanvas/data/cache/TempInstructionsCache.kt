package com.example.stringcanvas.data.cache

import android.util.Log

object TempInstructionsCache {
    private val cache = mutableMapOf<Long, TemporaryInstructionData>()

    fun put(id: Long, data: TemporaryInstructionData) {
        cache[id] = data
    }

//    fun get(id: Long): TemporaryInstructionData? = cache[id]

    fun get(id: Long): TemporaryInstructionData? {
        val result = cache[id]
        Log.d("Cache", "Get id=$id, exists=${result != null}")
        return result
    }

    // Удаляем инструкцию по ID из кэша
    fun remove(id: Long) {
        cache.remove(id)
    }

    // Очищаем весь кэш
    fun clear() {
        cache.clear()
    }

    // Очищаем инструкции по списку ID
    fun clear(vararg ids: Long) {
        ids.forEach { cache.remove(it) }
    }
}


//object TempInstructionsCache {
//    private val cache = mutableMapOf<Long, Instruction>()
//
//    // Сохраняем инструкцию в кэш
//    fun put(id: Long, instruction: Instruction) {
//        cache[id] = instruction
//    }
//
//    // Получаем инструкцию по ID из кэша
//    fun get(id: Long): Instruction? = cache[id]
//
//    // Удаляем инструкцию по ID из кэша
//    fun remove(id: Long) {
//        cache.remove(id)
//    }
//
//    // Очищаем весь кэш
//    fun clear() {
//        cache.clear()
//    }
//
//    // Очищаем инструкции по списку ID
//    fun clear(vararg ids: Long) {
//        ids.forEach { cache.remove(it) }
//    }
//}