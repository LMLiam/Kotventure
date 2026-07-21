package io.github.lmliam.kotventure.coroutines.prompt

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import net.kyori.adventure.audience.Audience
import kotlin.time.Duration

/**
 * Builds the component of one prompt.
 *
 * The component builder in `core` is internal to its module. Therefore, this scope delegates each component operation
 * to the builder that `component { }` supplies. It adds only the prompt operations.
 */
internal class PromptBuilder<T>(
    scope: ComponentScope,
    override val viewer: Audience,
    private val pending: PendingPrompt<T>,
    private val lifetime: Duration,
) : ComponentScope by scope,
    PromptScope<T> {
    internal var hasOptions: Boolean = false
        private set

    override fun option(
        value: T,
        init: ComponentScope.() -> Unit,
    ) {
        hasOptions = true
        append(
            component {
                init()
                click {
                    callback(
                        options = { lifetime(this@PromptBuilder.lifetime) },
                        function = { this@PromptBuilder.pending.answer(value) },
                    )
                }
            },
        )
    }
}
