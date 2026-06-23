package io.github.lmliam.kotventure.core.theme

/**
 * Returns the theme provider registered as [name], or null when none exists.
 */
public fun theme(name: String): ThemeProvider? = ThemeRegistry.theme(name)

/**
 * Returns the theme provider registered as the default theme, or null when none exists.
 */
public fun defaultTheme(): ThemeProvider? = ThemeRegistry.defaultTheme()
