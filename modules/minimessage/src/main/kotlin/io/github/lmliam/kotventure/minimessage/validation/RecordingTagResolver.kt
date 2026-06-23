package io.github.lmliam.kotventure.minimessage.validation

import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

internal class RecordingTagResolver(
    private val specNames: Set<String>,
) : TagResolver {
    val encounteredNames: LinkedHashSet<String> = LinkedHashSet()

    override fun resolve(
        name: String,
        arguments: ArgumentQueue,
        ctx: Context,
    ): Tag? {
        if (name in specNames || !TagResolver.standard().has(name)) {
            encounteredNames.add(name)
        }
        return null
    }

    override fun has(name: String): Boolean = true
}
