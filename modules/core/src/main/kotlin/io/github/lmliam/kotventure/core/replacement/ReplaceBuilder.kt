package io.github.lmliam.kotventure.core.replacement

import io.github.lmliam.kotventure.core.dsl.once
import io.github.lmliam.kotventure.core.text.TextBuilder
import io.github.lmliam.kotventure.core.text.TextScope
import io.github.lmliam.kotventure.core.text.text
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TextReplacementConfig
import java.util.regex.MatchResult
import java.util.regex.Pattern

private typealias ReplacementFactory = (MatchResult, TextComponent.Builder) -> ComponentLike?

private typealias ConditionApplier = TextReplacementConfig.Builder.() -> Unit

internal class ReplaceBuilder(
    private val pattern: Pattern,
) : ReplaceScope {
    private val namedGroups: Map<String, Int> = pattern.namedGroups()

    private var replacementFactory: ReplacementFactory? by
    once { "The replacement action is already set by 'modify', 'replacement', or 'remove'." }

    private var conditionApplier: ConditionApplier? by
    once { "The match condition is already set by 'once', 'times', or 'condition'." }

    private var insideHoverEvents: Boolean? by once { "'insideHoverEvents' is already set." }

    override fun replacement(
        value: String,
        init: TextScope.() -> Unit,
    ) {
        replacement(text(value, init))
    }

    override fun replacement(component: ComponentLike) {
        val prepared = component.asComponent()
        replacementFactory = { _, _ -> prepared }
    }

    override fun replacement(init: ReplacementScope.() -> ComponentLike?) {
        replacementFactory = { result, _ ->
            ReplacementState(result.snapshot()).init()
        }
    }

    override fun modify(init: ModifyScope.() -> Unit) {
        replacementFactory = { result, builder ->
            ModifyBuilder(
                text = TextBuilder(builder),
                match = result.snapshot(),
            ).apply(init).build()
        }
    }

    override fun remove() {
        replacementFactory = { _, _ -> null }
    }

    override fun once() {
        conditionApplier = { this.once() }
    }

    override fun times(count: Int) {
        require(count > 0) {
            "'times' must be positive, was $count."
        }

        conditionApplier = { this.times(count) }
    }

    override fun condition(predicate: ConditionScope.() -> MatchAction) {
        conditionApplier = {
            this.condition { result, matchCount, replacementCount ->
                ConditionState(
                    match = result.snapshot(),
                    matchCount = matchCount,
                    replacementCount = replacementCount,
                ).predicate().result
            }
        }
    }

    override fun insideHoverEvents(enabled: Boolean) {
        insideHoverEvents = enabled
    }

    fun build(): TextReplacementConfig {
        val replacement =
            checkNotNull(replacementFactory) {
                "A replacement action is required: use 'modify', 'replacement', or 'remove'."
            }

        val configBuilder = TextReplacementConfig.builder().match(pattern)

        configBuilder.replacement { result, textBuilder ->
            replacement(result, textBuilder)
        }
        conditionApplier?.invoke(configBuilder)
        insideHoverEvents?.let(configBuilder::replaceInsideHoverEvents)

        return configBuilder.build()
    }

    private fun MatchResult.snapshot(): TextMatch = TextMatch(this, namedGroups)
}
