package ph.project.active.domain

/** Deterministic, URL-safe ids for the in-memory store. */
internal class SequentialIds(private val prefix: String) {
    private var counter = 0
    fun next(): String = "$prefix-${++counter}"
}
