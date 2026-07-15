package io.github.lmliam.kotventure.paper.dialog.input

import io.github.lmliam.kotventure.core.dsl.once

internal class BooleanValuesBuilder : BooleanValuesScope {
    internal var onTrue: String? by once { "the 'true' value is already set." }
    internal var onFalse: String? by once { "the 'false' value is already set." }

    override operator fun Boolean.invoke(value: String) {
        if (this) {
            onTrue = value
        } else {
            onFalse = value
        }
    }
}
