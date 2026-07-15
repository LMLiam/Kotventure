package io.github.lmliam.kotventure.core.dsl

import kotlin.reflect.KProperty

/**
 * A write-once property delegate for nullable builder slots.
 *
 * Reads return `null` until the first assignment. The first assignment is accepted even when its
 * value is `null`; every later assignment fails.
 *
 * The default failure message includes the delegated property's name. Supply a custom message
 * when the delegate's property name differs from the public DSL slot name.
 *
 * This type is intentionally non-generic: its `getValue`/`setValue` operators are generic
 * instead, so `by once()` infers its type from the delegated property (the same mechanism as
 * `kotlin.properties.Delegates.notNull()`), and chains such as `by
 * once().inRange(1..1024)` (see [inRange]) infer without an explicit type argument.
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
 * The optional [alreadySetMessage] is evaluated only when a second assignment is attempted.
 *
 * @param alreadySetMessage custom duplicate-assignment message; otherwise the delegated property's
 *   name is used.
 */
@InternalKotventureApi
public fun once(alreadySetMessage: (() -> String)? = null): OnceAssign = OnceAssign(alreadySetMessage)
