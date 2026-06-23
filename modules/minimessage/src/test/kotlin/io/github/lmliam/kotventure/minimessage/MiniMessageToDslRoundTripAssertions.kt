package io.github.lmliam.kotventure.minimessage

import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

internal fun assertGoldenRoundTrip(
    input: String,
    expectedSource: String,
    expectedComponent: Component,
) {
    val parsed = mini(input)
    val generated = miniToDsl(input)

    generated shouldBe expectedSource
    MiniMessage.miniMessage().serialize(compileGeneratedDsl(generated)) shouldBe
        MiniMessage.miniMessage().serialize(expectedComponent)
    MiniMessage.miniMessage().serialize(parsed) shouldBe MiniMessage.miniMessage().serialize(expectedComponent)
}

internal fun assertGoldenRoundTrip(
    expectedSource: String,
    expectedComponent: Component,
) {
    val input = MiniMessage.miniMessage().serialize(expectedComponent)

    assertGoldenRoundTrip(input, expectedSource, expectedComponent)
}
