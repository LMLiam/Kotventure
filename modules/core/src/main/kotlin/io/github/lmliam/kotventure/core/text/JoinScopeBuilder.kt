package io.github.lmliam.kotventure.core.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration

internal class JoinScopeBuilder : JoinScope {
    private val builder = JoinConfiguration.builder()

    override fun separator(
        value: String,
        init: TextScope.() -> Unit,
    ) = separator(text(value, init))

    override fun separator(component: Component) {
        builder.separator(component)
    }

    override fun lastSeparator(
        value: String,
        init: TextScope.() -> Unit,
    ) = lastSeparator(text(value, init))

    override fun lastSeparator(component: Component) {
        builder.lastSeparator(component)
    }

    override fun prefix(
        value: String,
        init: TextScope.() -> Unit,
    ) = prefix(text(value, init))

    override fun prefix(component: Component) {
        builder.prefix(component)
    }

    override fun suffix(
        value: String,
        init: TextScope.() -> Unit,
    ) = suffix(text(value, init))

    override fun suffix(component: Component) {
        builder.suffix(component)
    }

    fun build(): JoinConfiguration = builder.build()
}
