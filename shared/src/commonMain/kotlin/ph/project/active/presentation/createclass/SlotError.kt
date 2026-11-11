package ph.project.active.presentation.createclass

sealed interface SlotError {
    data object InvalidSlot : SlotError
}
