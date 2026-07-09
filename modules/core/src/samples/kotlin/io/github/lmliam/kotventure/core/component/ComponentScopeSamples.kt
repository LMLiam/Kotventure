package io.github.lmliam.kotventure.core.component

import io.github.lmliam.kotventure.core.color.aqua
import io.github.lmliam.kotventure.core.keybind.keybind
import io.github.lmliam.kotventure.core.text.text

internal fun emptyComponentSample() {
    emptyComponent()
}

internal fun componentScopeSample() {
    component {
        text("Hello ") { color(aqua) }
        keybind("key.jump")
    }
}
