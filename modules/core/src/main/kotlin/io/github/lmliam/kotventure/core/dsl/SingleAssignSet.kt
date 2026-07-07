package io.github.lmliam.kotventure.core.dsl

import java.util.concurrent.ConcurrentHashMap

/**
 * A family of builder slots keyed by a typed value (such as a decoration), each assignable at most once.
 *
 * Claiming a key is atomic, so concurrent writers cannot both succeed for the same key.
 */
internal class SingleAssignSet<K : Any> {
    private val assigned = ConcurrentHashMap.newKeySet<K>()

    /**
     * Claims the slot for [key].
     *
     * @throws IllegalStateException when [key] was already assigned.
     */
    fun assign(key: K) {
        check(assigned.add(key)) { "'$key' is already set; it can only be set once per block." }
    }
}
