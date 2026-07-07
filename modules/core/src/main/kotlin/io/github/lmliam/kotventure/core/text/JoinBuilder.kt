package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.dsl.once
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.JoinConfiguration

internal class JoinBuilder : JoinScope {
    private val builder = JoinConfiguration.builder()
    private var separator: Component? by once()
    private var lastSeparator: Component? by once()
    private var prefix: Component? by once()
    private var suffix: Component? by once()

    override fun separator(
        value: String,
        init: TextScope.() -> Unit,
    ) = separator(text(value, init))

    override fun <T : ComponentLike> separator(component: T) {
        val value = component.asComponent()
        separator = value
        builder.separator(value)
    }

    override fun lastSeparator(
        value: String,
        init: TextScope.() -> Unit,
    ) = lastSeparator(text(value, init))

    override fun <T : ComponentLike> lastSeparator(component: T) {
        val value = component.asComponent()
        lastSeparator = value
        builder.lastSeparator(value)
    }

    override fun prefix(
        value: String,
        init: TextScope.() -> Unit,
    ) = prefix(text(value, init))

    override fun <T : ComponentLike> prefix(component: T) {
        val value = component.asComponent()
        prefix = value
        builder.prefix(value)
    }

    override fun suffix(
        value: String,
        init: TextScope.() -> Unit,
    ) = suffix(text(value, init))

    override fun <T : ComponentLike> suffix(component: T) {
        val value = component.asComponent()
        suffix = value
        builder.suffix(value)
    }

    internal fun build(): JoinConfiguration = builder.build()
}
