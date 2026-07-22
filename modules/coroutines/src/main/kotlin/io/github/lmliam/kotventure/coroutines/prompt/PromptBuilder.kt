package io.github.lmliam.kotventure.coroutines.prompt

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import net.kyori.adventure.audience.Audience
import kotlin.time.Duration

internal class PromptBuilder<T>(
    scope: ComponentScope,
    override val viewer: Audience,
    private val pending: PendingPrompt<T>,
    private val lifetime: Duration,
) : ComponentScope by scope,
    PromptScope<T> {
    internal var hasOption: Boolean = false
        private set

    override fun option(
        value: T,
        init: ComponentScope.() -> Unit,
    ) {
        hasOption = true
        append(
            component {
                init()
                click {
                    callback(
                        options = {
                            lifetime(this@PromptBuilder.lifetime)
                        },
                        function = {
                            this@PromptBuilder.pending.answer(value)
                        },
                    )
                }
            },
        )
    }
}
