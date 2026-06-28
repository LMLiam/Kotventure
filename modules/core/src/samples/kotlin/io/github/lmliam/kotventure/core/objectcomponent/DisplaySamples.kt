package io.github.lmliam.kotventure.core.objectcomponent

import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.text.text

internal fun displaySample() {
    val head = display(head("Alex")) { fallback(component { text("?") }) }
}
