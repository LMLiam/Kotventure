package io.github.lmliam.kotventure.core.component

import io.github.lmliam.kotventure.core.color.aqua
import io.github.lmliam.kotventure.core.keybind.keybind
import io.github.lmliam.kotventure.core.text.join
import io.github.lmliam.kotventure.core.text.text

internal fun emptyComponentSample() {
    emptyComponent()
}

internal fun newlineComponentSample() {
    listOf(text("line one"), text("line two")).join {
        separator(newlineComponent())
    }
}

internal fun componentScopeSample() {
    component {
        text("Hello ") { color(aqua) }
        keybind("key.jump")
    }
}
