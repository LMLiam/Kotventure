package io.github.lmliam.kotventure.minimessage.placeholder

private const val MINI_MESSAGE_TAG_NAME_PATTERN = "[!?#]?[a-z0-9_-]+"

private val miniMessageTagNameRegex =
    Regex(MINI_MESSAGE_TAG_NAME_PATTERN)

internal fun String.requireValidMiniMessageTagName() {
    require(matches(miniMessageTagNameRegex)) {
        "MiniMessage tag names must match $MINI_MESSAGE_TAG_NAME_PATTERN; received '$this'."
    }
}
