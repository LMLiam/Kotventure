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
 * Each registry owns its providers and default independently. Its operations are safe for concurrent callers.
 * [register] fails on duplicate names. [replace] and [unregister] support reload operations that replace or
 * remove a theme after startup. Prefer unregistering with the
 * theme object (`themes.unregister(Brand)`) when it is available.
 */
public class ThemeRegistry {
    private val lock = ReentrantLock()
    private val providers = mutableMapOf<String, ThemeProvider>()
    private var defaultProvider: ThemeProvider? = null

    /**
     * This registry's default theme provider, or `null` when none exists.
     */
    public val defaultTheme: ThemeProvider?
        get() = lock.withLock { defaultProvider }

    /**
     * Registers [provider], optionally marking it as this registry's default theme.
     *
     * @return [provider]
     * @throws IllegalArgumentException when the provider name is blank or already registered.
     * @throws IllegalStateException when [default] is true and this registry already has a default
     * theme.
     */
    public fun <T : ThemeProvider> register(
        provider: T,
        default: Boolean = false,
    ): T {
        val providerName = provider.requireName()

        lock.withLock {
            require(providerName !in providers) {
                "Theme provider '$providerName' is already registered."
            }

            val currentDefault = defaultProvider
            check(!default || currentDefault == null) {
                "Default theme provider '${currentDefault?.name}' is already registered."
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
     * [default] is `true`, [provider] becomes the sole default. When [default] is `false`,
     * replacement of the current default clears the default. A different theme that is already
     * the default does not change.
     *
     * @return [provider]
     * @throws IllegalArgumentException when the provider name is blank.
     */
    public fun <T : ThemeProvider> replace(
        provider: T,
        default: Boolean = false,
    ): T {
        val providerName = provider.requireName()

        lock.withLock {
            val previous = providers.put(providerName, provider)

            when {
                default -> defaultProvider = provider
                previous === defaultProvider -> defaultProvider = null
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
     * @throws IllegalArgumentException when the provider name is blank.
     */
    public fun <T : ThemeProvider> unregister(provider: T): T? {
        val providerName = provider.requireName()

        return lock.withLock {
            provider
                .takeIf { providers[providerName] === provider }
                ?.also { removeRegistered(providerName) }
        }
    }

    /**
     * Removes the theme registered as [name].
     *
     * Prefer [unregister] with the theme object when available. Use this overload for dynamic lookup and
     * interoperability when only the name is known.
     *
     * When the removed provider was the default theme, the default is cleared.
     *
     * @return the removed provider, or null when [name] was not registered.
     * @throws IllegalArgumentException when [name] is blank.
     */
    public fun unregister(name: String): ThemeProvider? {
        val themeName = name.requireName()

        return lock.withLock {
            removeRegistered(themeName)
        }
    }

    /**
     * Returns the theme provider registered as [name], or `null` when none exists.
     *
     * @throws IllegalArgumentException when [name] is blank.
     */
    public operator fun get(name: String): ThemeProvider? {
        val themeName = name.requireName()

        return lock.withLock {
            providers[themeName]
        }
    }

    /**
     * Returns the theme provider registered as [name], or `null` when none exists.
     *
     * @throws IllegalArgumentException when [name] is blank.
     */
    public fun theme(name: String): ThemeProvider? = get(name)

    /**
     * Returns this registry's default theme provider, or `null` when none exists.
     */
    public fun defaultTheme(): ThemeProvider? = defaultTheme

    private fun removeRegistered(name: String): ThemeProvider? =
        providers.remove(name)?.also { removed ->
            if (defaultProvider === removed) {
                defaultProvider = null
            }
        }

    private fun ThemeProvider.requireName(): String = name.requireName()

    private fun String.requireName(): String =
        also {
            require(it.isNotBlank()) { "Theme provider name must not be blank." }
        }
}
