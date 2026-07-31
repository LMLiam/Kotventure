package io.github.lmliam.kotventure.core.replacement

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import java.util.regex.Pattern

/**
 * Replaces each match of [literal] inside this component, and returns the changed component.
 *
 * [literal] is a literal string. It is not a regular expression, unlike Adventure's own `match(String)` method on
 * `TextReplacementConfig.Builder`. [init] configures the replacement action, the match limit, and the hover-event
 * behaviour.
 *
 * This function only builds a new component. It does not change this component.
 *
 * @sample io.github.lmliam.kotventure.core.replacement.replaceLiteralSample
 *
 * @param literal the literal text to find.
 * @param init configures the replacement action, the match limit, and the hover-event behaviour.
 * @throws IllegalStateException when [init] sets a write-once slot twice, or sets no replacement action.
 * @throws IllegalArgumentException when [init] calls `times` with a count below `1`.
 */
public fun Component.replace(
    literal: String,
    init: ReplaceScope.() -> Unit,
): Component = replaceText(buildReplacement(Pattern.compile(literal, Pattern.LITERAL), init))

/**
 * Replaces each match of [pattern] inside this component, and returns the changed component.
 *
 * [init] configures the replacement action, the match limit, and the hover-event behaviour.
 *
 * This function only builds a new component. It does not change this component.
 *
 * @sample io.github.lmliam.kotventure.core.replacement.replaceModifySample
 *
 * @param pattern the regular expression to find.
 * @param init configures the replacement action, the match limit, and the hover-event behaviour.
 * @throws IllegalStateException when [init] sets a write-once slot twice, or sets no replacement action.
 * @throws IllegalArgumentException when [init] calls `times` with a count below `1`.
 */
public fun Component.replace(
    pattern: Regex,
    init: ReplaceScope.() -> Unit,
): Component = replaceText(buildReplacement(pattern.toPattern(), init))

internal fun buildReplacement(
    pattern: Pattern,
    init: ReplaceScope.() -> Unit,
): TextReplacementConfig = ReplaceBuilder(pattern).apply(init).build()
