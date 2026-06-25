package io.github.lmliam.kotventure.core.text

import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.JoinConfiguration

internal class JoinBuilder : JoinScope {
    private val builder = JoinConfiguration.builder()

    override fun separator(
        value: String,
        init: TextScope.() -> Unit,
    ) = separator(text(value, init))

    override fun <T : ComponentLike> separator(component: T) {
        builder.separator(component.asComponent())
    }

    override fun lastSeparator(
        value: String,
        init: TextScope.() -> Unit,
    ) = lastSeparator(text(value, init))

    override fun <T : ComponentLike> lastSeparator(component: T) {
        builder.lastSeparator(component.asComponent())
    }

    override fun prefix(
        value: String,
        init: TextScope.() -> Unit,
    ) = prefix(text(value, init))

    override fun <T : ComponentLike> prefix(component: T) {
        builder.prefix(component.asComponent())
    }

    override fun suffix(
        value: String,
        init: TextScope.() -> Unit,
    ) = suffix(text(value, init))

    override fun <T : ComponentLike> suffix(component: T) {
        builder.suffix(component.asComponent())
    }

    internal fun build(): JoinConfiguration = builder.build()
}
