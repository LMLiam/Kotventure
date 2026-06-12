package io.github.lmliam.kotventure.core.theme

import io.github.lmliam.kotventure.core.registry.AdventureDsl

/**
 * Returns the theme provider registered as [name], or null when none exists.
 */
public fun theme(name: String): ThemeProvider? = AdventureDsl.theme(name)

/**
 * Returns the theme provider registered as the default theme, or null when none exists.
 */
public fun defaultTheme(): ThemeProvider? = AdventureDsl.defaultTheme()
