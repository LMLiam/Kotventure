package io.github.lmliam.kotventure.core.theme

import net.kyori.adventure.text.format.Style

/**
 * Provides named Adventure styles for one design system or theme.
 */
public interface ThemeProvider {
    /**
     * Registration name for this provider.
     *
     * A [ThemeRegistry] requires it to be non-blank and unique within that registry.
     */
    public val name: String

    /**
     * Returns the [Style] registered as [name], or `null` when this provider does not define it.
     */
    public fun style(name: String): Style?
}
