package io.github.lmliam.kotventure.core.score

import io.github.lmliam.kotventure.core.component.ComponentBuilder
import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.text.Component

/**
 * Creates a score [Component]. The client resolves it to the value of [objective] for [name] at display time.
 * This function only creates the component. It does not read a scoreboard or send the component to an audience.
 *
 * @sample io.github.lmliam.kotventure.core.score.scoreSample
 *
 * @param name the score holder, for example a player name or a selector such as `"@s"`.
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
    ComponentBuilder(
        Component.score().name(name).objective(objective),
    ).apply(init).build()

/**
 * Creates a score component and appends it as the next child of this scope.
 *
 * @param name the score holder, for example a player name or a selector such as `"@s"`.
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
