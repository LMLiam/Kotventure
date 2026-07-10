package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.component.emptyComponent
import io.github.lmliam.kotventure.core.dsl.once
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

internal class TabListBuilder : TabListScope {
    private var header: Component? by once()
    private var footer: Component? by once()

    override fun header(init: ComponentScope.() -> Unit) = header(component(init))

    override fun <T : ComponentLike> header(component: T) {
        header = component.asComponent()
    }

    override fun footer(init: ComponentScope.() -> Unit) = footer(component(init))

    override fun <T : ComponentLike> footer(component: T) {
        footer = component.asComponent()
    }

    internal fun sendTo(audience: Audience) {
        check(header != null || footer != null) {
            "At least one of 'header' or 'footer' must be set."
        }
        audience.sendPlayerListHeaderAndFooter(header ?: emptyComponent(), footer ?: emptyComponent())
    }
}
