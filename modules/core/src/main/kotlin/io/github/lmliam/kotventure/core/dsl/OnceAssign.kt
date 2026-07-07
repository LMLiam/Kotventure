package io.github.lmliam.kotventure.core.dsl

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A builder slot that may be assigned at most once; the failure message reuses the delegated property's
 * name, so a slot's DSL name and its diagnostic cannot drift apart.
 */
internal class OnceAssign<T> : ReadWriteProperty<Any?, T?> {
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
        check(!assigned) { "'${property.name}' is already set." }
        assigned = true
        this.value = value
    }
}

/** Creates a [OnceAssign] slot for a `by`-delegated builder property. */
internal fun <T> once(): OnceAssign<T> = OnceAssign()
