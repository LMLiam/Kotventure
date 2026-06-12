package io.github.lmliam.kotventure.core.style

import net.kyori.adventure.text.format.Style

/**
 * Builds a reusable Adventure [Style] from a Kotventure style DSL block.
 */
public fun style(init: StyleScope.() -> Unit): Style =
    Style
        .style()
        .also { builder -> StyleBuilder(builder).init() }
        .build()
