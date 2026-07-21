package io.github.lmliam.kotventure.core.event

import kotlin.time.Duration.Companion.minutes

internal fun clickSample() {
    val link = click { openUrl("https://example.com") }
}

internal fun clickActionScopeSample() {
    click { openUrl("https://example.com") }
    click { run("/spawn") }
}

internal fun hoverSample() {
    val tooltip = hover { text("Click to teleport") }
}

internal fun clickOptionsSample() {
    val singleUse = clickOptions { uses(1) }
    val briefButton =
        clickOptions {
            uses(unlimited)
            lifetime(10.minutes)
        }
}
