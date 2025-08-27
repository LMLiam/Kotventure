package text.style

import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.TextColor

/**
 * Base colour palette.
 *
 * Consumers can subclass this and override values to provide
 * their own brand/theme colours
 */
interface Palette {
    val black: TextColor
        get() = BLACK
    val darkBlue: TextColor
        get() = DARK_BLUE
    val darkGreen: TextColor
        get() = DARK_GREEN
    val darkAqua: TextColor
        get() = DARK_AQUA
    val darkRed: TextColor
        get() = DARK_RED
    val darkPurple: TextColor
        get() = DARK_PURPLE
    val gold: TextColor
        get() = GOLD
    val gray: TextColor
        get() = GRAY
    val darkGray: TextColor
        get() = DARK_GRAY
    val blue: TextColor
        get() = BLUE
    val green: TextColor
        get() = GREEN
    val aqua: TextColor
        get() = AQUA
    val red: TextColor
        get() = RED
    val lightPurple: TextColor
        get() = LIGHT_PURPLE
    val yellow: TextColor
        get() = YELLOW
    val white: TextColor
        get() = WHITE
}