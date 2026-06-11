package io.github.lmliam.kotventure.core.color

import net.kyori.adventure.text.format.NamedTextColor

/**
 * Standard Adventure `black` color.
 */
public val black: NamedTextColor = NamedTextColor.BLACK

/**
 * Standard Adventure `dark_blue` color.
 */
public val darkBlue: NamedTextColor = NamedTextColor.DARK_BLUE

/**
 * Standard Adventure `dark_green` color.
 */
public val darkGreen: NamedTextColor = NamedTextColor.DARK_GREEN

/**
 * Standard Adventure `dark_aqua` color.
 */
public val darkAqua: NamedTextColor = NamedTextColor.DARK_AQUA

/**
 * Standard Adventure `dark_red` color.
 */
public val darkRed: NamedTextColor = NamedTextColor.DARK_RED

/**
 * Standard Adventure `dark_purple` color.
 */
public val darkPurple: NamedTextColor = NamedTextColor.DARK_PURPLE

/**
 * Standard Adventure `gold` color.
 */
public val gold: NamedTextColor = NamedTextColor.GOLD

/**
 * Standard Adventure `gray` color.
 */
public val gray: NamedTextColor = NamedTextColor.GRAY

/**
 * Standard Adventure `dark_gray` color.
 */
public val darkGray: NamedTextColor = NamedTextColor.DARK_GRAY

/**
 * Standard Adventure `blue` color.
 */
public val blue: NamedTextColor = NamedTextColor.BLUE

/**
 * Standard Adventure `green` color.
 */
public val green: NamedTextColor = NamedTextColor.GREEN

/**
 * Standard Adventure `aqua` color.
 */
public val aqua: NamedTextColor = NamedTextColor.AQUA

/**
 * Standard Adventure `red` color.
 */
public val red: NamedTextColor = NamedTextColor.RED

/**
 * Standard Adventure `light_purple` color.
 */
public val lightPurple: NamedTextColor = NamedTextColor.LIGHT_PURPLE

/**
 * Standard Adventure `yellow` color.
 */
public val yellow: NamedTextColor = NamedTextColor.YELLOW

/**
 * Standard Adventure `white` color.
 */
public val white: NamedTextColor = NamedTextColor.WHITE

/**
 * Finds a standard Adventure named color by its exact Adventure name, such as `dark_blue`.
 */
public fun namedColor(name: String): NamedTextColor? = NamedTextColor.NAMES.value(name)

/**
 * Finds a standard Adventure named color by its exact Adventure name, or throws if [name] is unknown.
 */
public fun namedColorOrThrow(name: String): NamedTextColor = NamedTextColor.NAMES.valueOrThrow(name)
