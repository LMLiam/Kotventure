package io.github.lmliam.kotventure.minimessage.conversion

import net.kyori.adventure.text.`object`.ObjectContents
import net.kyori.adventure.text.`object`.PlayerHeadObjectContents
import net.kyori.adventure.text.`object`.SpriteObjectContents

/**
 * Returns an object-contents expression for [contents].
 *
 * A sprite in Adventure's default atlas uses the one-argument `sprite` call. Player-head contents use the applicable
 * `head` call.
 */
internal fun objectContentsLiteral(contents: ObjectContents): String =
    when (contents) {
        is SpriteObjectContents -> contents.toDslLiteral()
        is PlayerHeadObjectContents -> contents.toDslLiteral()
    }

private fun SpriteObjectContents.toDslLiteral(): String {
    val sprite = keyLiteral(sprite())

    return if (atlas() == SpriteObjectContents.DEFAULT_ATLAS) {
        "sprite($sprite)"
    } else {
        "sprite(${keyLiteral(atlas())}, $sprite)"
    }
}

/**
 * Returns the lossless `head` expression for this player-head content.
 *
 * The converter requires one skin source and no profile properties. It rejects other states because the DSL cannot
 * represent them without loss.
 */
private fun PlayerHeadObjectContents.toDslLiteral(): String {
    if (profileProperties().isNotEmpty()) {
        conversionError(
            "miniToDsl cannot represent player-head profile properties: the <head> tag does not set them.",
        )
    }

    val skinSource = skinSourceLiteral()
    val hatArgument = if (hat()) "" else ", hat = false"

    return "head($skinSource$hatArgument)"
}

private fun PlayerHeadObjectContents.skinSourceLiteral(): String =
    listOfNotNull(
        name()?.let(::quoted),
        id()?.let { "uuid(${quoted(it.toString())})" },
        texture()?.let(::keyLiteral),
    ).singleOrNull()
        ?: conversionError(
            "miniToDsl cannot represent a player head without exactly one skin source (a name, UUID, or texture).",
        )
