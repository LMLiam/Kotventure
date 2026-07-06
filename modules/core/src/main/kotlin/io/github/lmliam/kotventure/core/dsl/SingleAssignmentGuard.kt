package io.github.lmliam.kotventure.core.dsl

/**
 * Tracks which singleton slots of a builder block have been assigned, so a second assignment fails fast
 * instead of silently overwriting the first.
 */
internal class SingleAssignmentGuard {
    private val assigned = mutableSetOf<String>()

    /**
     * Records the slot [name] as assigned.
     *
     * @throws IllegalStateException when [name] was already assigned in this block.
     */
    fun assign(name: String) {
        check(assigned.add(name)) { "'$name' is already set; it can only be set once per block." }
    }
}
