package io.github.lmliam.kotventure.core.score

import io.github.lmliam.kotventure.core.component.ComponentScopeBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ScoreComponent

internal class ScoreComponentBuilder(
    name: String,
    objective: String,
) : ComponentScopeBuilder<ScoreComponent, ScoreComponent.Builder>(
    Component.score().name(name).objective(objective),
),
    ScoreScope
