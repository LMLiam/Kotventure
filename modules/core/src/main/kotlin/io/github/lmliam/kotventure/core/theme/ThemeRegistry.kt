package io.github.lmliam.kotventure.core.theme

/**
 * Explicit registry for dynamic theme lookup.
 *
 * Direct Kotlin callers should prefer compile-checked theme properties such as `Brand.header`.
 * This registry exists for dynamic lookup and interop cases where the theme name is only known
 * at runtime.
 */
public class ThemeRegistry {
    private val lock = Any()
    private var providers: Map<String, ThemeProvider> = emptyMap()
    private var defaultProvider: ThemeProvider? = null

    /**
     * Registers [provider], optionally marking it as this registry's default theme.
     *
     * @throws IllegalArgumentException when the provider name is blank, already registered, or
     * when [default] is true and this registry already has a default theme.
     */
    public fun <T : ThemeProvider> register(
        provider: T,
        default: Boolean = false,
    ): T {
        require(provider.name.isNotBlank()) { "Theme provider name must not be blank." }
        synchronized(lock) {
            require(provider.name !in providers) {
                "Theme provider '${provider.name}' is already registered."
            }
            require(!default || defaultProvider == null) {
                "Default theme provider '${checkNotNull(defaultProvider).name}' is already registered."
            }
            providers = providers + (provider.name to provider)
            if (default) {
                defaultProvider = provider
            }
        }
        return provider
    }

    /**
     * Returns the theme provider registered as [name], or null when none exists.
     */
    public fun theme(name: String): ThemeProvider? =
        synchronized(lock) {
            providers[name]
        }

    /**
     * Returns the theme provider registered as this registry's default theme, or null when none
     * exists.
     */
    public fun defaultTheme(): ThemeProvider? =
        synchronized(lock) {
            defaultProvider
        }
}
