package io.github.lmliam.kotventure.core.theme

import net.kyori.adventure.text.format.Style

/**
 * Provides named Adventure styles for a design system or theme.
 */
public interface ThemeProvider {
    /**
     * Unique registration name for this theme provider.
     */
    public val name: String

    /**
     * Returns the named [Style], or null when the theme does not define it.
     */
    public fun style(name: String): Style?
}
