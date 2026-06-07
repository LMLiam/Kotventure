package io.github.lmliam.kotventure.core.minimessage

/**
 * Describes a named MiniMessage tag provider registered with Kotventure.
 *
 * The MiniMessage module adapts this extension point to Adventure's concrete tag resolver type.
 */
public interface MiniMessageTagProvider {
    /**
     * Unique registration name for this tag provider.
     */
    public val name: String
}
