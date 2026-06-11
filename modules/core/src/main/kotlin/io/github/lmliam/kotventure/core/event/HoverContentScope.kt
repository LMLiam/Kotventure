package io.github.lmliam.kotventure.core.event

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.text.TextScope
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.event.DataComponentValue
import java.util.UUID

/**
 * Scope for selecting the single payload shown by a hover event.
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
     * Selects an item hover payload from [key], [count], and typed [dataComponents].
     *
     * @throws IllegalArgumentException when [count] is negative.
     */
    public fun item(
        key: Key,
        count: Int = 1,
        dataComponents: Map<Key, DataComponentValue> = emptyMap(),
    )

    /**
     * Selects an item hover payload from [item], [count], and typed [dataComponents].
     *
     * @throws IllegalArgumentException when [count] is negative.
     */
    public fun item(
        item: Keyed,
        count: Int = 1,
        dataComponents: Map<Key, DataComponentValue> = emptyMap(),
    ) {
        item(item.key(), count, dataComponents)
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
