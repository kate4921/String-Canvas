package com.example.stringcanvas.domain.usecase


import com.example.stringcanvas.domain.repository.InstructionRepository

class RenameInstructionUseCase(
    private val repo: InstructionRepository
) {
    suspend operator fun invoke(id: Long, newName: String) {
        require(newName.isNotBlank()) { "Name cannot be blank" }
        repo.updateName(id, newName.trim())
    }
}