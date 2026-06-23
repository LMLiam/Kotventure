package io.github.lmliam.kotventure.core.theme

internal object ThemeRegistry {
    private val lock = Any()
    private var providers: Map<String, ThemeProvider> = emptyMap()
    private var defaultName: String? = null

    fun register(
        provider: ThemeProvider,
        default: Boolean,
    ) {
        require(provider.name.isNotBlank()) { "Theme provider name must not be blank." }
        synchronized(lock) {
            require(provider.name !in providers) {
                "Theme provider '${provider.name}' is already registered."
            }
            providers = providers + (provider.name to provider)
            if (default) {
                defaultName = provider.name
            }
        }
    }

    fun theme(name: String): ThemeProvider? =
        synchronized(lock) {
            providers[name]
        }

    fun defaultTheme(): ThemeProvider? =
        synchronized(lock) {
            defaultName?.let(providers::get)
        }

    fun reset() {
        synchronized(lock) {
            providers = emptyMap()
            defaultName = null
        }
    }
}
