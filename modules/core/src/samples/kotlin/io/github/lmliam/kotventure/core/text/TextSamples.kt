package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.component.component

internal fun textSample() {
    val greeting =
        text("Hello") {
            color(gold)
            bold()
        }
}

internal fun stringInvokeSample() {
    component {
        "Hello" {
            color(gold)
            bold()
        }
    }
}

internal fun stringUnaryPlusSample() {
    component {
        +"Hello"
    }
}
