package io.github.lmliam.kotventure.core.text

import net.kyori.adventure.text.TextComponent

internal fun componentSequenceSample() {
    val root = text("Hello")
    val mentionsAlex = root.asSequence().any { it is TextComponent && "Alex" in it.content() }
}
