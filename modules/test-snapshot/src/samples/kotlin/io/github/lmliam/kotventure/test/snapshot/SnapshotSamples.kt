package io.github.lmliam.kotventure.test.snapshot

import io.github.lmliam.kotventure.core.text.text
import net.kyori.adventure.text.Component

internal fun shouldMatchSnapshotSample() {
    val component: Component = text("Welcome")
    component shouldMatchSnapshot "welcome-message"
}
