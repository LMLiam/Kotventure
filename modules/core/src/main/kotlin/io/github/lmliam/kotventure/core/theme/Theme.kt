package io.github.lmliam.kotventure.core.theme

import io.github.lmliam.kotventure.core.style.StyleScope
import net.kyori.adventure.text.format.Style
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import io.github.lmliam.kotventure.core.style.style as buildStyle

/**
 * Base class for design-system themes declared as Kotlin objects.
 *
 * Declare semantic styles as delegated properties. The compiler then checks references such as `Brand.header`. The
 * property names also become keys for dynamic [ThemeProvider.style] lookup. The object records styles in declaration
 * order. Thus, declare palette properties before the styles that use them.
 *
 * @sample io.github.lmliam.kotventure.core.theme.themeSample
 *
 * Runtime lookups require explicit registration: add the theme to a [ThemeRegistry] during
 * startup. Use [ThemeRegistry.replace] later to reload it. A theme declaration does not register the theme.
 */
public abstract class Theme(
    public override val name: String,
) : ThemeProvider {
    private val styles: LinkedHashMap<String, Style> = LinkedHashMap()

    init {
        require(name.isNotBlank()) { "Theme name must not be blank." }
    }

    public override fun style(name: String): Style? = styles[name]

    /**
     * Returns an immutable snapshot of the declared styles keyed by semantic name, in
     * declaration order.
     */
    public fun styles(): Map<String, Style> =
        buildMap {
            putAll(styles)
        }

    /**
     * Declares a semantic style property whose name doubles as its dynamic lookup key.
     *
     * @throws IllegalArgumentException when the key is already declared by this theme.
     */
    protected fun style(init: StyleScope.() -> Unit): PropertyDelegateProvider<Theme, ReadOnlyProperty<Theme, Style>> =
        PropertyDelegateProvider { theme, property ->
            val styleKey = property.name
            require(styleKey.isNotBlank()) { "Theme style name must not be blank." }
            val built = buildStyle(init)
            require(theme.styles.put(styleKey, built) == null) {
                "Duplicate style '$styleKey' in theme '${theme.name}'."
            }
            ReadOnlyProperty { _, _ -> built }
        }
}
