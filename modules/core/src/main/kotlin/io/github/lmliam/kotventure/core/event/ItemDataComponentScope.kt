package io.github.lmliam.kotventure.core.event

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.nbt.NbtCompoundScope
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.event.DataComponentValue

/**
 * Scope for declaring the data components carried by an item hover payload.
 *
 * [HoverContentScope.item] opens this scope. Each call adds the component that its [Key] identifies. The scope keeps
 * declaration order. It throws [IllegalStateException] if a key occurs more than one time. This rule applies to
 * values and to [removed] markers.
 */
@KotventureDslMarker
public interface ItemDataComponentScope {
    /**
     * Adds the data component at [key], with its value authored as compound NBT via [init].
     *
     * @throws IllegalStateException when [key] is already declared in this block.
     */
    public fun component(
        key: Key,
        init: NbtCompoundScope.() -> Unit,
    )

    /**
     * Adds the data component at [key] from a prebuilt [value], such as a raw `nbt("...")` holder.
     *
     * @throws IllegalStateException when [key] is already declared in this block.
     */
    public fun component(
        key: Key,
        value: DataComponentValue,
    )

    /**
     * Marks the data component at [key] for removal.
     *
     * @throws IllegalStateException when [key] is already declared in this block.
     */
    public fun removed(key: Key)
}
