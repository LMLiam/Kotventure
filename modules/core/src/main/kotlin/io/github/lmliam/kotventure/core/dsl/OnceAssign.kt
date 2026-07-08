package io.github.lmliam.kotventure.core.dsl

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A builder slot that may be assigned at most once.
 *
 * The failure message uses [slotName] when provided, otherwise the delegated property's name, so a
 * slot's DSL name and its diagnostic cannot drift apart — even when the backing property must use a
 * different name (e.g. it collides with a scope-bound val).
 */
internal class OnceAssign<T>(
    private val slotName: String? = null,
) : ReadWriteProperty<Any?, T?> {
    private var assigned = false
    private var value: T? = null

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): T? = value

    /**
     * @throws IllegalStateException when the property was already assigned.
     */
    override fun setValue(
        thisRef: Any?,
        property: KProperty<*>,
        value: T?,
    ) {
        check(!assigned) { "'${slotName ?: property.name}' is already set." }
        assigned = true
        this.value = value
    }
}

/**
 * Creates a [OnceAssign] slot for a `by`-delegated builder property.
 *
 * @param slotName optional DSL slot name for the failure message when the property name is not the
 *   public slot name.
 */
internal fun <T> once(slotName: String? = null): OnceAssign<T> = OnceAssign(slotName)
