package com.example.stringcanvas.presentation.screens.instructionVariant

sealed class InstructionVariantScreenEvent {
    data class LoadInstructions(val id1: Long, val id2: Long) : InstructionVariantScreenEvent()
    data class ImageClicked(val chosenId: Long, val otherId: Long) : InstructionVariantScreenEvent()
    data class BackClicked(val id1: Long, val id2: Long) : InstructionVariantScreenEvent()
}
