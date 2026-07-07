package io.github.lmliam.kotventure.core.dsl

import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A builder slot that may be assigned at most once; the failure message reuses the delegated property's
 * name, so a slot's DSL name and its diagnostic cannot drift apart.
 *
 * Assignment claims the slot atomically, so concurrent writers cannot both succeed.
 */
internal class SingleAssign<T> : ReadWriteProperty<Any?, T?> {
    private val assigned = AtomicBoolean(false)

    @Volatile
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
        check(assigned.compareAndSet(false, true)) {
            "'${property.name}' is already set; it can only be set once per block."
        }
        this.value = value
    }
}

/** Creates a [SingleAssign] slot for a `by`-delegated builder property. */
internal fun <T> singleAssign(): SingleAssign<T> = SingleAssign()
