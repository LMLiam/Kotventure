package io.github.lmliam.kotventure.minimessage.validation

import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/**
 * A [TagResolver] that records the names of tags it is asked to resolve.
 *
 * [has] returns `true` for all names so that Adventure calls [resolve] for every `<tag>` in
 * the input. [resolve] records the name when either:
 * - [TagResolver.standard] does not claim it (it's a custom/unknown tag), or
 * - it appears in [specNames] (a spec placeholder whose name collides with a standard tag).
 *
 * This dual condition prevents standard formatting tags (e.g. `<red>`, `<bold>`) from polluting
 * the recorded set, while still recording spec-named placeholders that share a standard tag
 * name (e.g. a placeholder declared as `"gold"`).
 *
 * [resolve] returns `null` so the lenient parser silently skips each tag.
 *
 * Tags are recorded in encounter order (the order [resolve] is first called for each name).
 *
 * @param specNames the set of placeholder names declared in the spec; used to force-record
 *   names that would otherwise be filtered by the standard-tag check.
 */
internal class RecordingTagResolver(
    private val specNames: Set<String>,
) : TagResolver {
    /** Tag names encountered in the input, in the order they were first seen. */
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
