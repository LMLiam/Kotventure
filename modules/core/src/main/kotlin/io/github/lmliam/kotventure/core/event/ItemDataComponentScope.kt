package io.github.lmliam.kotventure.core.event

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.nbt.NbtCompoundScope
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.event.DataComponentValue

/**
 * Scope for declaring the data components carried by an item hover payload.
 *
 * Opened by the trailing lambda of [HoverContentScope.item]. Each call adds the component identified by
 * its [Key]; declaring the same key twice — whether added or [removed] — throws [IllegalStateException].
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
     * Adds the data component at [key] from a pre-built [value], such as a raw `nbt("...")` holder.
     *
     * @throws IllegalStateException when [key] is already declared in this block.
     */
    public fun component(
        key: Key,
        value: DataComponentValue,
    )

    /**
     * Marks the data component at [key] for removal, mirroring the top-level `removed()` marker.
     *
     * @throws IllegalStateException when [key] is already declared in this block.
     */
    public fun removed(key: Key)
}
