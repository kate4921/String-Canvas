package com.example.stringcanvas.data.database

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.stringcanvas.domain.models.Instruction

@Entity(tableName = "instructions")
data class InstructionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val textList: List<String>,
    val nailsCount: Int = 0,
    val linesCount: Int = 0,
    val imageUriString: String? = null,
    val bookmark: Int = 0
)

fun Instruction.toEntity(): InstructionEntity {
    return InstructionEntity(
        id = this.id,
        name = this.name,
        textList = this.textList,
        nailsCount = this.nailsCount,
        linesCount = this.linesCount,
        imageUriString = this.imageUri?.toString(),
        bookmark = this.bookmark
    )
}

fun InstructionEntity.toDomain(): Instruction {
    return Instruction(
        id = this.id,
        name = this.name,
        textList = this.textList,
        nailsCount = this.nailsCount,
        linesCount = this.linesCount,
        imageUri = this.imageUriString?.let { Uri.parse(it) },
        bookmark = this.bookmark
    )
}
