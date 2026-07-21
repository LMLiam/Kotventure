package io.github.lmliam.kotventure.core.dsl

import kotlin.reflect.KProperty

/**
 * A write-once property delegate for nullable builder slots.
 *
 * Reads return `null` until the first assignment. The first assignment is accepted even when its
 * value is `null`. Each later assignment fails.
 *
 * The default failure message includes the delegated property's name. Supply a custom message
 * when the delegate's property name differs from the public DSL slot name.
 *
 * The operator functions infer their type from the delegated property. Validation combinators such as [inRange]
 * preserve this inference.
 */
@InternalKotventureApi
public class OnceAssign internal constructor(
    internal val alreadySetMessage: (() -> String)? = null,
) {
    private var assigned = false
    private var value: Any? = null

    /**
     * Returns the assigned value, or `null` before the slot has been assigned.
     */
    @Suppress("UNCHECKED_CAST")
    public operator fun <T> getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): T? = value as T?

    /**
     * Assigns the slot once.
     *
     * @throws IllegalStateException when the slot has already been assigned.
     */
    public operator fun <T> setValue(
        thisRef: Any?,
        property: KProperty<*>,
        value: T?,
    ) {
        check(!assigned) {
            alreadySetMessage?.invoke() ?: "'${property.name}' is already set."
        }

        assigned = true
        this@OnceAssign.value = value
    }
}

/**
 * Creates a write-once delegate for a nullable builder property.
 *
 * The optional [alreadySetMessage] is evaluated only when a second assignment occurs. Creating the delegate does not
 * evaluate it or change builder state.
 *
 * @param alreadySetMessage the custom duplicate-assignment message. If it is absent, the function uses the delegated
 *   property's name.
 */
@InternalKotventureApi
public fun once(alreadySetMessage: (() -> String)? = null): OnceAssign = OnceAssign(alreadySetMessage)
