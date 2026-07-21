package io.github.lmliam.kotventure.core.style

import net.kyori.adventure.text.format.Style

/**
 * Creates an immutable [Style] from [init] for reuse across components.
 *
 * The function does not modify or display a component.
 *
 * @throws IllegalStateException when [init] assigns the same style attribute or decoration more than once.
 * @sample io.github.lmliam.kotventure.core.style.styleSample
 */
public fun style(init: StyleScope.() -> Unit): Style =
    Style
        .style()
        .also { builder -> StyleBuilder(builder).init() }
        .build()
