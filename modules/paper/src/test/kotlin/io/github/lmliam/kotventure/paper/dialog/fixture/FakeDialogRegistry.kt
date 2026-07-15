package io.github.lmliam.kotventure.paper.dialog.fixture

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.tag.Tag
import io.papermc.paper.registry.tag.TagKey
import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import java.util.stream.Stream

/**
 * Minimal [Registry] returning a single placeholder [Dialog] for any lookup, so Paper's [Dialog]
 * static constants resolve during class initialization. Only the lookup paths the constants use
 * are implemented; everything else is unused.
 */
internal class FakeDialogRegistry(
    private val dialog: Dialog,
) : Registry<Dialog> {
    override fun get(key: NamespacedKey): Dialog = dialog

    override fun getOrThrow(key: Key): Dialog = dialog

    override fun getKey(value: Dialog): NamespacedKey = error("not used")

    override fun hasTag(key: TagKey<Dialog>): Boolean = error("not used")

    override fun getTag(key: TagKey<Dialog>): Tag<Dialog> = error("not used")

    override fun getTags(): Collection<Tag<Dialog>> = error("not used")

    override fun stream(): Stream<Dialog> = error("not used")

    override fun keyStream(): Stream<NamespacedKey> = error("not used")

    override fun size(): Int = 0

    override fun iterator(): MutableIterator<Dialog> = error("not used")
}
