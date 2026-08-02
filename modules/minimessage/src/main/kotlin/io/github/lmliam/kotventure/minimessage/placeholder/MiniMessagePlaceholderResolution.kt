package io.github.lmliam.kotventure.minimessage.placeholder

import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

internal fun <T : Any> MiniMessagePlaceholder<T>.toTagResolver(value: T): TagResolver =
    if (acceptsComponents) {
        Placeholder.component(
            name,
            value.requireComponentValue(),
        )
    } else {
        Placeholder.unparsed(
            name,
            value.toString(),
        )
    }

private fun Any.requireComponentValue(): ComponentLike =
    this as? ComponentLike
        ?: throw IllegalArgumentException(
            "Component placeholder received ${this::class.displayName}.",
        )
