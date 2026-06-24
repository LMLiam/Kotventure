package io.github.lmliam.kotventure.core.score

import io.github.lmliam.kotventure.core.component.ComponentBuilder
import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ScoreComponent

/**
 * Builds a score [Component] — text the client resolves to a scoreboard value at display time.
 *
 * ```kotlin
 * val kills = score(name = "@s", objective = "kills")
 * ```
 *
 * @param name the scoreholder, e.g. a player name or selector like `"@s"`.
 * @param objective the scoreboard objective to read.
 * @param init styles the component and appends any children.
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
 * Appends a score child to this scope, for use inside a `component { }` or other component block.
 *
 * @param name the scoreholder, e.g. a player name or selector like `"@s"`.
 * @param objective the scoreboard objective to read.
 * @param init styles the child and appends any of its own children.
 */
public fun ComponentScope.score(
    name: String,
    objective: String,
    init: ComponentScope.() -> Unit = {},
) {
    append(buildScoreComponent(name, objective, init))
}
