package io.github.lmliam.kotventure.core.event

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.nbt.NbtCompoundScope
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.event.DataComponentValue

/**
 * Scope for declaring the data components carried by an item hover payload.
 *
 * Opened by the trailing lambda of [HoverContentScope.item]. Each call adds the component identified by
 * its [Key]; a later call for the same key replaces the earlier one.
 */
@KotventureDslMarker
public interface ItemDataComponentScope {
    /**
     * Adds the data component at [key], with its value authored as compound NBT via [init].
     */
    public fun component(
        key: Key,
        init: NbtCompoundScope.() -> Unit,
    )

    /**
     * Adds the data component at [key] from a pre-built [value], such as a raw `nbt("...")` holder.
     */
    public fun component(
        key: Key,
        value: DataComponentValue,
    )

    /**
     * Marks the data component at [key] for removal, mirroring the top-level `removed()` marker.
     */
    public fun removed(key: Key)
}
