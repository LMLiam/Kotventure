package io.github.lmliam.kotventure.core.score

import io.github.lmliam.kotventure.core.component.ComponentBuilder
import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ScoreComponent

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
): Component =
    ComponentBuilder<ScoreComponent, ScoreComponent.Builder>(
        Component.score().name(name).objective(objective),
    ).apply(init).build()

/**
 * Appends a nested score child with [name] and [objective].
 */
public fun ComponentScope.score(
    name: String,
    objective: String,
    init: ComponentScope.() -> Unit = {},
) {
    append(buildScoreComponent(name, objective, init))
}
