package io.github.lmliam.kotventure.core.registry

import io.github.lmliam.kotventure.core.animation.AnimationDriver
import io.github.lmliam.kotventure.core.minimessage.MiniMessageTagProvider
import io.github.lmliam.kotventure.core.platform.PlatformAdapter
import io.github.lmliam.kotventure.core.theme.ThemeProvider

/**
 * Explicit startup registry for Kotventure extension points.
 *
 * Register extensions during application startup before concurrent use. The registry stores
 * MiniMessage tag providers, theme providers, animation drivers, and a single active platform
 * adapter without classpath scanning. Concrete MiniMessage resolver adaptation belongs to the
 * MiniMessage module so `core` depends only on Adventure API.
 */
public object AdventureDsl {
    private val miniMessageTagProviders: MutableMap<String, MiniMessageTagProvider> = mutableMapOf()
    private val themeProviders: MutableMap<String, ThemeProvider> = mutableMapOf()
    private var defaultThemeName: String? = null
    private val animationDrivers: MutableMap<String, AnimationDriver> = mutableMapOf()
    private var platformAdapter: PlatformAdapter? = null

    /**
     * Registers [provider] as the active MiniMessage tag provider for its name.
     */
    public fun registerMiniMessageTag(provider: MiniMessageTagProvider) {
        miniMessageTagProviders[provider.name] = provider
    }

    /**
     * Returns the MiniMessage tag provider registered as [name], or null when none exists.
     */
    public fun miniMessageTag(name: String): MiniMessageTagProvider? = miniMessageTagProviders[name]

    /**
     * Returns an immutable snapshot of registered MiniMessage tag providers by name.
     */
    public fun miniMessageTags(): Map<String, MiniMessageTagProvider> = miniMessageTagProviders.immutableSnapshot()

    /**
     * Registers [provider] as the active theme provider for its name, optionally marking it as
     * the [default] theme.
     */
    public fun registerTheme(
        provider: ThemeProvider,
        default: Boolean = false,
    ) {
        themeProviders[provider.name] = provider
        if (default) {
            defaultThemeName = provider.name
        }
    }

    /**
     * Returns the theme provider registered as [name], or null when none exists.
     */
    public fun theme(name: String): ThemeProvider? = themeProviders[name]

    /**
     * Returns the theme provider registered as the default theme, or null when none exists.
     */
    public fun defaultTheme(): ThemeProvider? = defaultThemeName?.let(::theme)

    /**
     * Returns an immutable snapshot of registered theme providers by name.
     */
    public fun themes(): Map<String, ThemeProvider> = themeProviders.immutableSnapshot()

    /**
     * Registers [driver] as the active animation driver for its name.
     */
    public fun registerAnimationDriver(driver: AnimationDriver) {
        animationDrivers[driver.name] = driver
    }

    /**
     * Returns the animation driver registered as [name], or null when none exists.
     */
    public fun animationDriver(name: String): AnimationDriver? = animationDrivers[name]

    /**
     * Returns an immutable snapshot of registered animation drivers by name.
     */
    public fun animationDrivers(): Map<String, AnimationDriver> = animationDrivers.immutableSnapshot()

    /**
     * Registers [adapter] as the active platform adapter.
     */
    public fun registerPlatformAdapter(adapter: PlatformAdapter) {
        platformAdapter = adapter
    }

    /**
     * Returns the active platform adapter, or null when no platform has registered one.
     */
    public fun platformAdapter(): PlatformAdapter? = platformAdapter

    internal fun reset() {
        miniMessageTagProviders.clear()
        themeProviders.clear()
        defaultThemeName = null
        animationDrivers.clear()
        platformAdapter = null
    }

    private fun <K, V> Map<K, V>.immutableSnapshot(): Map<K, V> =
        buildMap {
            putAll(this@immutableSnapshot)
        }
}
