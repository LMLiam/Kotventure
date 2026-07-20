package io.github.lmliam.kotventure.core.color

import net.kyori.adventure.text.format.NamedTextColor

/**
 * The standard Adventure `black` colour.
 */
public val black: NamedTextColor = NamedTextColor.BLACK

/**
 * The standard Adventure `dark_blue` colour.
 */
public val darkBlue: NamedTextColor = NamedTextColor.DARK_BLUE

/**
 * The standard Adventure `dark_green` colour.
 */
public val darkGreen: NamedTextColor = NamedTextColor.DARK_GREEN

/**
 * The standard Adventure `dark_aqua` colour.
 */
public val darkAqua: NamedTextColor = NamedTextColor.DARK_AQUA

/**
 * The standard Adventure `dark_red` colour.
 */
public val darkRed: NamedTextColor = NamedTextColor.DARK_RED

/**
 * The standard Adventure `dark_purple` colour.
 */
public val darkPurple: NamedTextColor = NamedTextColor.DARK_PURPLE

/**
 * The standard Adventure `gold` colour.
 */
public val gold: NamedTextColor = NamedTextColor.GOLD

/**
 * The standard Adventure `gray` colour.
 */
public val gray: NamedTextColor = NamedTextColor.GRAY

/**
 * The standard Adventure `dark_gray` colour.
 */
public val darkGray: NamedTextColor = NamedTextColor.DARK_GRAY

/**
 * The standard Adventure `blue` colour.
 */
public val blue: NamedTextColor = NamedTextColor.BLUE

/**
 * The standard Adventure `green` colour.
 */
public val green: NamedTextColor = NamedTextColor.GREEN

/**
 * The standard Adventure `aqua` colour.
 */
public val aqua: NamedTextColor = NamedTextColor.AQUA

/**
 * The standard Adventure `red` colour.
 */
public val red: NamedTextColor = NamedTextColor.RED

/**
 * The standard Adventure `light_purple` colour.
 */
public val lightPurple: NamedTextColor = NamedTextColor.LIGHT_PURPLE

/**
 * The standard Adventure `yellow` colour.
 */
public val yellow: NamedTextColor = NamedTextColor.YELLOW

/**
 * The standard Adventure `white` colour.
 */
public val white: NamedTextColor = NamedTextColor.WHITE

/**
 * Finds a standard Adventure named colour by its exact Adventure name, such as `dark_blue`.
 */
public fun namedColor(name: String): NamedTextColor? = NamedTextColor.NAMES.value(name)

/**
 * Finds a standard Adventure named colour by its exact Adventure name.
 *
 * @throws NoSuchElementException if [name] is unknown.
 */
public fun namedColorOrThrow(name: String): NamedTextColor = NamedTextColor.NAMES.valueOrThrow(name)
