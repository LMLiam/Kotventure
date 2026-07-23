package io.github.lmliam.kotventure.core.replacement

import io.github.lmliam.kotventure.core.dsl.once
import io.github.lmliam.kotventure.core.text.TextBuilder
import io.github.lmliam.kotventure.core.text.TextScope
import io.github.lmliam.kotventure.core.text.text
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TextReplacementConfig
import java.util.function.BiFunction
import java.util.regex.MatchResult
import java.util.regex.Pattern

internal class ReplaceBuilder(
    private val pattern: Pattern,
) : ReplaceScope {
    private val namedGroups: Map<String, Int> = pattern.namedGroups()

    private var replacementFactory: ((MatchResult, TextComponent.Builder) -> ComponentLike?)? by
        once { "The replacement action is already set by 'modify', 'replacement', or 'remove'." }
    private var conditionApplier: ((TextReplacementConfig.Builder) -> Unit)? by
        once { "The match condition is already set by 'once', 'times', or 'condition'." }
    private var insideHoverEvents: Boolean? by once { "'insideHoverEvents' is already set." }

    override fun replacement(
        value: String,
        init: TextScope.() -> Unit,
    ) {
        replacement(text(value, init))
    }

    override fun <T : ComponentLike> replacement(component: T) {
        val prepared = component.asComponent()
        replacementFactory = { _, _ -> prepared }
    }

    override fun replacement(build: ReplacementScope.() -> ComponentLike?) {
        replacementFactory = { result, _ -> ReplacementState(TextMatch(result, namedGroups)).build() }
    }

    override fun modify(build: ModifyScope.() -> Unit) {
        replacementFactory = { result, builder ->
            ModifyBuilder(TextBuilder(builder), TextMatch(result, namedGroups)).apply(build).build()
        }
    }

    override fun remove() {
        replacementFactory = { _, _ -> null }
    }

    override fun once() {
        conditionApplier = { it.once() }
    }

    override fun times(count: Int) {
        require(count > 0) { "'times' must be positive, was $count." }
        conditionApplier = { it.times(count) }
    }

    override fun condition(predicate: ConditionScope.() -> MatchAction) {
        conditionApplier = { builder ->
            builder.condition { result, matchCount, replaced ->
                ConditionState(TextMatch(result, namedGroups), matchCount, replaced).predicate().result
            }
        }
    }

    override fun insideHoverEvents(replace: Boolean) {
        insideHoverEvents = replace
    }

    fun build(): TextReplacementConfig {
        val factory =
            checkNotNull(replacementFactory) {
                "A replacement action is required: use 'modify', 'replacement', or 'remove'."
            }

        val configBuilder = TextReplacementConfig.builder().match(pattern)
        val replacementFunction =
            BiFunction<MatchResult, TextComponent.Builder, ComponentLike?> { result, builder ->
                factory(result, builder)
            }
        configBuilder.replacement(replacementFunction)
        conditionApplier?.invoke(configBuilder)
        insideHoverEvents?.let(configBuilder::replaceInsideHoverEvents)

        return configBuilder.build()
    }
}
