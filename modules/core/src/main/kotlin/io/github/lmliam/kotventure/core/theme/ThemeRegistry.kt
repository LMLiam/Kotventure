package io.github.lmliam.kotventure.core.theme

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Explicit registry for dynamic theme lookup.
 *
 * Direct Kotlin callers should prefer compile-checked theme properties such as `Brand.header`.
 * This registry exists for dynamic lookup and interop cases where the theme name is only known
 * at runtime.
 *
 * Lifecycle: [register] fails on duplicate names; [replace] and [unregister] support hot-reload
 * workflows where a theme is swapped or removed after startup. Prefer unregistering with the
 * theme object (`themes.unregister(Brand)`) when it is available.
 */
public class ThemeRegistry {
    private val lock = ReentrantLock()
    private val providers: MutableMap<String, ThemeProvider> = mutableMapOf()
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
        val providerName = requireProviderName(provider)
        lock.withLock {
            require(providerName !in providers) {
                "Theme provider '$providerName' is already registered."
            }
            require(!default || defaultProvider == null) {
                "Default theme provider '${checkNotNull(defaultProvider).name}' is already registered."
            }
            providers[providerName] = provider
            if (default) {
                defaultProvider = provider
            }
        }
        return provider
    }

    /**
     * Registers [provider], replacing any existing provider with the same name.
     *
     * Use this for hot-reload: a second registration of the same name is intentional. When
     * [default] is `true`, [provider] becomes the sole default (any previous default is cleared).
     * When [default] is `false` and this replaces the previous default under the same name, the
     * default is cleared; a different theme that was already default is left unchanged.
     *
     * @return [provider]
     * @throws IllegalArgumentException when the provider name is blank.
     */
    public fun <T : ThemeProvider> replace(
        provider: T,
        default: Boolean = false,
    ): T {
        val providerName = requireProviderName(provider)
        lock.withLock {
            val previous = providers.put(providerName, provider)
            when {
                default -> defaultProvider = provider
                previous != null && defaultProvider === previous -> defaultProvider = null
            }
        }
        return provider
    }

    /**
     * Removes [provider] when it is the instance currently registered under [ThemeProvider.name].
     *
     * Prefer this overload when the theme object is known (`themes.unregister(Brand)`). After
     * [replace] has installed a different instance under the same name, this is a no-op.
     *
     * When the removed provider was the default theme, the default is cleared.
     *
     * @return [provider] when it was removed, or null when it is not the registered instance.
     */
    public fun <T : ThemeProvider> unregister(provider: T): T? =
        lock.withLock {
            val current = providers[provider.name]
            if (current !== provider) {
                return@withLock null
            }
            removeRegistered(provider.name, provider)
            provider
        }

    /**
     * Removes the theme registered as [name].
     *
     * Prefer [unregister] with the theme object when available; this overload is for dynamic
     * lookup and interop where only the name is known.
     *
     * When the removed provider was the default theme, the default is cleared.
     *
     * @return the removed provider, or null when [name] was not registered.
     */
    public fun unregister(name: String): ThemeProvider? =
        lock.withLock {
            val removed = providers[name] ?: return@withLock null
            removeRegistered(name, removed)
            removed
        }

    /**
     * Returns the theme provider registered as [name], or null when none exists.
     */
    public fun theme(name: String): ThemeProvider? =
        lock.withLock {
            providers[name]
        }

    /**
     * Returns the theme provider registered as this registry's default theme, or null when none
     * exists.
     */
    public fun defaultTheme(): ThemeProvider? =
        lock.withLock {
            defaultProvider
        }

    private fun removeRegistered(
        name: String,
        removed: ThemeProvider,
    ) {
        providers.remove(name)
        if (defaultProvider === removed) {
            defaultProvider = null
        }
    }

    private fun requireProviderName(provider: ThemeProvider): String {
        val providerName = provider.name
        require(providerName.isNotBlank()) { "Theme provider name must not be blank." }
        return providerName
    }
}
