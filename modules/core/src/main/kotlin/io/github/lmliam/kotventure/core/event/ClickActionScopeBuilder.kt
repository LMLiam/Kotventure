package io.github.lmliam.kotventure.core.event

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import kotlin.time.toJavaDuration
import kotlin.time.Duration as KotlinDuration

internal class ClickActionScopeBuilder : ClickActionScope {
    private var selected = false
    private var event: ClickEvent<*>? = null

    override fun open(target: String) {
        set(openTarget(target))
    }

    override fun openUrl(url: String) {
        set(ClickEvent.openUrl(url))
    }

    override fun openFile(file: String) {
        set(ClickEvent.openFile(file))
    }

    override fun run(command: String) {
        set(ClickEvent.runCommand(command))
    }

    override fun suggest(command: String) {
        set(ClickEvent.suggestCommand(command))
    }

    override fun changePage(page: Int) {
        set(ClickEvent.changePage(page))
    }

    override fun copy(text: String) {
        set(ClickEvent.copyToClipboard(text))
    }

    override fun callback(function: ClickCallback<Audience>) {
        set(ClickEvent.callback(function))
    }

    override fun callback(
        uses: Int,
        lifetime: KotlinDuration,
        function: ClickCallback<Audience>,
    ) {
        set(
            ClickEvent.callback(function) { options ->
                options.uses(uses)
                options.lifetime(lifetime.toJavaDuration())
            },
        )
    }

    override fun callback(
        options: ClickCallback.Options,
        function: ClickCallback<Audience>,
    ) {
        set(ClickEvent.callback(function, options))
    }

    internal fun build(): ClickEvent<*> =
        event
            ?: error(
                "click { ... } must choose exactly one action with open(...), openUrl(...), openFile(...), " +
                        "run(...), suggest(...), changePage(...), copy(...), or callback(...).",
            )

    private fun set(event: ClickEvent<*>) {
        check(!selected) {
            "click { ... } must choose only one action: open(...), openUrl(...), openFile(...), run(...), " +
                    "suggest(...), changePage(...), copy(...), or callback(...)."
        }
        selected = true
        this.event = event
    }
}
