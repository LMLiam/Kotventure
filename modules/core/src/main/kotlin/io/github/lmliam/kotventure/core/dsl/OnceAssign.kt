package io.github.lmliam.kotventure.core.dsl

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A builder slot that may be assigned at most once.
 *
 * By default the failure message reuses the delegated property's name, so a slot's DSL name and its
 * diagnostic cannot drift apart. Pass [alreadySetMessage] when the backing property name is not the
 * public slot name (e.g. a scope-bound val occupies that name).
 */
internal class OnceAssign<T>(
    private val alreadySetMessage: (() -> String)? = null,
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
        check(!assigned) {
            alreadySetMessage?.invoke() ?: "'${property.name}' is already set."
        }
        assigned = true
        this.value = value
    }
}

/**
 * Creates a [OnceAssign] slot for a `by`-delegated builder property.
 *
 * @param alreadySetMessage optional failure message when the slot is assigned a second time;
 *   defaults to `"'{property.name}' is already set."`.
 */
internal fun <T> once(alreadySetMessage: (() -> String)? = null): OnceAssign<T> = OnceAssign(alreadySetMessage)
