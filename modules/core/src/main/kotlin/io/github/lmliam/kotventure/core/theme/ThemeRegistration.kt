package io.github.lmliam.kotventure.core.theme

import io.github.lmliam.kotventure.core.registry.AdventureDsl

/**
 * Registers this theme provider with the explicit [AdventureDsl] registry and returns it,
 * optionally marking it as the [default] theme.
 */
public fun <T : ThemeProvider> T.register(default: Boolean = false): T =
    apply {
        AdventureDsl.registerTheme(this, default = default)
    }
