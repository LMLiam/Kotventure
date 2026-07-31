package io.github.lmliam.kotventure.minimessage.validation

import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/**
 * Records declared placeholder tags and non-standard custom tags encountered during a lenient parse.
 */
internal class PlaceholderRecordingResolver(
    private val declaredNames: Set<String>,
    private val standardTags: TagResolver,
) : TagResolver {
    private val encountered =
        linkedSetOf<String>()

    internal val encounteredNames: Set<String>
        get() = encountered

    override fun resolve(
        name: String,
        arguments: ArgumentQueue,
        ctx: Context,
    ): Tag? {
        if (name in declaredNames || !standardTags.has(name)) {
            encountered += name
        }

        return null
    }

    /**
     * Every tag must reach [resolve] so it can be recorded before another resolver handles it.
     */
    override fun has(name: String): Boolean = true
}
