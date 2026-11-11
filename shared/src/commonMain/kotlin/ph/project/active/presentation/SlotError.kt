package ph.project.active.presentation

sealed interface SlotError {
    data object InvalidSlot : SlotError
}
