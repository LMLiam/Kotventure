package io.github.lmliam.kotventure.core.dsl

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A write-once property delegate for nullable builder slots that additionally validates every
 * non-null assignment.
 *
 * Reads return `null` until the first assignment. The double-set check runs first: a second
 * assignment always fails with [IllegalStateException], even if the new value would itself be
 * valid. [validate] then runs on non-null values. A rejected value does not consume the slot, so a later valid
 * assignment succeeds.
 *
 * Constructed by combinators such as [inRange] and [positive] on [OnceAssign]. Do not construct it directly.
 */
@InternalKotventureApi
public class ValidatedOnceAssign<T> internal constructor(
    private val alreadySetMessage: (() -> String)?,
    private val validate: (name: String, value: T & Any) -> Unit,
) : ReadWriteProperty<Any?, T?> {
    private var assigned = false
    private var value: T? = null

    /**
     * Returns the assigned value, or `null` before the slot has been assigned.
     */
    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): T? = value

    /**
     * Assigns the slot once.
     *
     * A `null` assignment consumes the slot. The function validates a non-null [value] first. A rejected value does not
     * consume the slot.
     *
     * @throws IllegalStateException when the slot has already been assigned.
     * @throws IllegalArgumentException when [validate] rejects a non-null [value].
     */
    override fun setValue(
        thisRef: Any?,
        property: KProperty<*>,
        value: T?,
    ) {
        check(!assigned) {
            alreadySetMessage?.invoke() ?: "'${property.name}' is already set."
        }

        if (value != null) {
            validate(property.name, value)
        }

        assigned = true
        this@ValidatedOnceAssign.value = value
    }
}
