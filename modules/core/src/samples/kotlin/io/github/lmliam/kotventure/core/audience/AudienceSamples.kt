package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.text.text

internal fun emptyAudienceSample() {
    val nobody = emptyAudience()

    nobody.message { text("never rendered") }
}

internal fun audienceOfSample() {
    val team = audienceOf(emptyAudience(), emptyAudience())

    team.message { text("delivered to every member") }
}
