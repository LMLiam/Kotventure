package io.github.lmliam.kotventure.core.theme

import io.github.lmliam.kotventure.core.registry.AdventureDsl

/**
 * Registers this theme provider with Kotventure's startup registry and returns it, optionally
 * marking it as the [default] theme.
 *
 * Registering another provider with the same name replaces the previous registration.
 *
 * @throws IllegalArgumentException when the provider name is blank.
 */
public fun <T : ThemeProvider> T.register(default: Boolean = false): T =
    apply {
        AdventureDsl.registerTheme(this, default = default)
    }
