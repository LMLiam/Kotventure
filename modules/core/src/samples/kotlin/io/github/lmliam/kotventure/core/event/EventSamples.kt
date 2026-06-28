package io.github.lmliam.kotventure.core.event

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
