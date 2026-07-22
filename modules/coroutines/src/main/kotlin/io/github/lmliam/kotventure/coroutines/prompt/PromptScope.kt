package io.github.lmliam.kotventure.coroutines.prompt

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.audience.Audience

/**
 * Configures the question text and the clickable options of one prompt.
 *
 * The scope is a [ComponentScope]. Thus, the complete component DSL is available for the question and for each option
 * label. Declare an option with [option]. Each other operation appends text around the options.
 *
 * @param T the type that each option resumes with.
 * @sample io.github.lmliam.kotventure.coroutines.prompt.askSample
 */
@KotventureDslMarker
public interface PromptScope<T> : ComponentScope {
    /**
     * The audience that receives this prompt.
     *
     * Use this property to show different options to different audiences. The prompt block runs one time for each
     * [ask][io.github.lmliam.kotventure.coroutines.prompt.ask]. Therefore, a prompt value stays correct for each
     * audience.
     *
     * Kotventure scopes use one DSL marker. Therefore, a nested block masks this property. Inside an [option] block,
     * use a label such as `this@ask.viewer`. As an alternative, read the property into a local value before the block.
     *
     * @sample io.github.lmliam.kotventure.coroutines.prompt.promptValueSample
     */
    public val viewer: Audience

    /**
     * Appends a clickable option that resumes the prompt with [value].
     *
     * [init] builds the option label. The option applies its own click event. Thus, the label block must not apply
     * one. The first option that a player clicks resumes the prompt. A later click does nothing.
     *
     * @throws IllegalStateException when [init] applies a click event, or assigns another write-once slot two times.
     */
    public fun option(
        value: T,
        init: ComponentScope.() -> Unit,
    )
}
