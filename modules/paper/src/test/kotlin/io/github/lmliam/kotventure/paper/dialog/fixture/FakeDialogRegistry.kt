package io.github.lmliam.kotventure.paper.dialog.fixture

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.tag.Tag
import io.papermc.paper.registry.tag.TagKey
import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import java.util.stream.Stream

/**
 * A minimal [Registry] that returns one placeholder [Dialog] for each lookup.
 *
 * This registry lets Paper resolve static [Dialog] constants during class initialisation. It implements only the
 * lookup paths that these constants use.
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
