package io.github.lmliam.kotventure.core.style

import net.kyori.adventure.text.format.Style

/**
 * Builds a reusable [Style] that can be applied to many components.
 *
 * @sample io.github.lmliam.kotventure.core.style.styleSample
 */
public fun style(init: StyleScope.() -> Unit): Style =
    Style
        .style()
        .also { builder -> StyleBuilder(builder).init() }
        .build()
