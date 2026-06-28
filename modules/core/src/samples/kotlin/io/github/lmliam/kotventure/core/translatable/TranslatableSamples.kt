package io.github.lmliam.kotventure.core.translatable

internal fun translatableSample() {
    val died =
        translatable("death.attack.player") {
            arg { content("Alex") }
            fallback("Alex was slain")
        }
}
