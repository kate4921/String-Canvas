package com.example.stringcanvas.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stringcanvas.domain.models.InstructionPreview

@Dao
interface InstructionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstruction(entity: InstructionEntity): Long

    @Query("SELECT * FROM instructions WHERE id = :id")
    suspend fun getInstructionById(id: Long): InstructionEntity?

    @Query("DELETE FROM instructions WHERE id = :id")
    suspend fun deleteInstructionById(id: Long)

    @Query("SELECT * FROM instructions")
    suspend fun getSavedInstructions(): List<InstructionEntity>

    @Query("UPDATE instructions SET name = :newName WHERE id = :id")
    suspend fun updateInstructionName(id: Long, newName: String)

    /* --- превью для списка --- */
    @Query("SELECT id, name, imageUriString FROM instructions")
    suspend fun getInstructionPreviews(): List<InstructionPreview>
}