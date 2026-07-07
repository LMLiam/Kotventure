package io.github.lmliam.kotventure.core.dsl

import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A builder slot that may be assigned at most once; the failure message reuses the delegated property's
 * name, so a slot's DSL name and its diagnostic cannot drift apart.
 */
internal class OnceAssign<T> : ReadWriteProperty<Any?, T?> {
    private val value = AtomicReference<Any?>(Unset)

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): T? {
        val current = value.get()
        return if (current === Unset) {
            null
        } else {
            @Suppress("UNCHECKED_CAST")
            current as T?
        }
    }

    /**
     * @throws IllegalStateException when the property was already assigned.
     */
    override fun setValue(
        thisRef: Any?,
        property: KProperty<*>,
        value: T?,
    ) {
        check(this.value.compareAndSet(Unset, value)) { "'${property.name}' is already set." }
    }

    private object Unset
}

/** Creates a [OnceAssign] slot for a `by`-delegated builder property. */
internal fun <T> once(): OnceAssign<T> = OnceAssign()
