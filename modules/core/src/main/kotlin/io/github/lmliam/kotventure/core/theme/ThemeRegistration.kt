package io.github.lmliam.kotventure.core.theme

/**
 * Registers this theme provider and returns it, optionally marking it as the [default] theme.
 *
 * @throws IllegalArgumentException when the provider name is blank or already registered.
 */
public fun <T : ThemeProvider> T.register(default: Boolean = false): T =
    apply {
        ThemeRegistry.register(this, default = default)
    }
