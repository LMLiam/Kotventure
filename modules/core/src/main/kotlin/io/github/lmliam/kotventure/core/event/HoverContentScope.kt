package io.github.lmliam.kotventure.core.event

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.text.TextScope
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import net.kyori.adventure.text.ComponentLike
import java.util.UUID

/**
 * Selects the payload for a hover event.
 *
 * Select exactly one payload in each `hover { }` block. The block throws [IllegalStateException] if it selects no
 * payload or more than one payload.
 */
@KotventureDslMarker
public interface HoverContentScope {
    /**
     * Selects a text hover payload from an existing [component].
     */
    public fun text(component: ComponentLike)

    /**
     * Selects a text hover payload from [value], configured by [init].
     */
    public fun text(
        value: String,
        init: TextScope.() -> Unit = {},
    )

    /**
     * Selects a text hover payload from rich component DSL content.
     */
    public fun text(init: ComponentScope.() -> Unit)

    /**
     * Selects an item hover payload from [key] and [count], with its data components declared by [components].
     *
     * @throws IllegalArgumentException when [count] is negative.
     * @throws IllegalStateException when [components] declares the same data-component key more than one time.
     */
    public fun item(
        key: Key,
        count: Int = 1,
        components: ItemDataComponentScope.() -> Unit = {},
    )

    /**
     * Selects an item hover payload from [item] and [count], with its data components declared by [components].
     *
     * @throws IllegalArgumentException when [count] is negative.
     * @throws IllegalStateException when [components] declares the same data-component key more than one time.
     */
    public fun item(
        item: Keyed,
        count: Int = 1,
        components: ItemDataComponentScope.() -> Unit = {},
    ) {
        item(item.key(), count, components)
    }

    /**
     * Selects an entity hover payload from [type], [id], and optional [name].
     */
    public fun entity(
        type: Key,
        id: UUID,
        name: ComponentLike? = null,
    )

    /**
     * Selects an entity hover payload from [type], [id], and a DSL-built name.
     */
    public fun entity(
        type: Key,
        id: UUID,
        init: ComponentScope.() -> Unit,
    )

    /**
     * Selects an entity hover payload from [type], [id], and optional [name].
     */
    public fun entity(
        type: Keyed,
        id: UUID,
        name: ComponentLike? = null,
    ) {
        entity(type.key(), id, name)
    }

    /**
     * Selects an entity hover payload from [type], [id], and a DSL-built name.
     */
    public fun entity(
        type: Keyed,
        id: UUID,
        init: ComponentScope.() -> Unit,
    ) {
        entity(type.key(), id, init)
    }
}
