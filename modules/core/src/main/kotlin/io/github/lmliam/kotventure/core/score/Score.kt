package io.github.lmliam.kotventure.core.score

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.addChild
import net.kyori.adventure.text.Component

/**
 * Builds an Adventure score [Component] from a Kotventure DSL block.
 */
public fun score(
    name: String,
    objective: String,
    init: ComponentScope.() -> Unit = {},
): Component = buildScoreComponent(name, objective, init)

internal fun buildScoreComponent(
    name: String,
    objective: String,
    init: ComponentScope.() -> Unit = {},
): Component = ScoreComponentBuilder(name, objective).apply(init).build()

/**
 * Appends a nested score child with [name] and [objective].
 */
public fun ComponentScope.score(
    name: String,
    objective: String,
    init: ComponentScope.() -> Unit = {},
) {
    addChild(buildScoreComponent(name, objective, init))
}
