package io.github.lmliam.kotventure.core.component

import net.kyori.adventure.text.Component

internal interface ComponentChildren {
    fun addChild(component: Component)
}

internal fun ComponentScope.addChild(component: Component) {
    check(this is ComponentChildren) {
        "Nested component DSL functions can only be used inside Kotventure component builders."
    }
    addChild(component)
}
