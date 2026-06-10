package io.github.lmliam.kotventure.core.score

import net.kyori.adventure.text.Component

/**
 * Builds an Adventure score [Component] from a Kotventure DSL block.
 */
public fun score(
    name: String,
    objective: String,
    init: ScoreScope.() -> Unit = {},
): Component = ScoreComponentBuilder(name, objective).apply(init).build()
