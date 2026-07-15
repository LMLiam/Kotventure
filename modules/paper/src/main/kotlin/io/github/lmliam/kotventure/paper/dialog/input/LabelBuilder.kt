package io.github.lmliam.kotventure.paper.dialog.input

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.once

internal class LabelBuilder(
    delegate: ComponentScope,
) : LabelScope,
    ComponentScope by delegate {
    internal var visible: Boolean? by once()

    override fun visible(value: Boolean) {
        visible = value
    }
}
