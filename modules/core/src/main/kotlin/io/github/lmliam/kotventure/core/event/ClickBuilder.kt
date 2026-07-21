package io.github.lmliam.kotventure.core.event

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent

internal class ClickBuilder : ClickActionScope {
    private var event: ClickEvent<*>? = null

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

    override fun callback(
        options: ClickOptionsScope.() -> Unit,
        function: ClickCallback<Audience>,
    ) {
        set(ClickEvent.callback(function, clickOptions(options)))
    }

    internal fun build(): ClickEvent<*> =
        event
            ?: error(
                "click { ... } must choose exactly one action with openUrl(...), openFile(...), " +
                        "run(...), suggest(...), changePage(...), copy(...), or callback(...).",
            )

    private fun set(event: ClickEvent<*>) {
        check(this.event == null) {
            "click { ... } must choose only one action: openUrl(...), openFile(...), run(...), " +
                    "suggest(...), changePage(...), copy(...), or callback(...)."
        }
        this.event = event
    }
}
