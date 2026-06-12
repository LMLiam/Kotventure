package io.github.lmliam.kotventure.core.theme

import io.github.lmliam.kotventure.core.style.StyleScope
import net.kyori.adventure.text.format.Style
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import io.github.lmliam.kotventure.core.style.style as buildStyle

/**
 * Base class for design-system themes declared as Kotlin objects.
 *
 * Semantic styles are declared as delegated properties so references like `Brand.header` are
 * compile-checked, while the property names double as the keys served through the dynamic
 * [ThemeProvider.style] lookup. Styles are recorded in declaration order during object
 * initialization, so palette properties must be declared before the styles that use them.
 *
 * ```kotlin
 * object Brand : Theme("brand") {
 *     val primary = hex("#5865F2")
 *
 *     val header: Style by style {
 *         color(primary)
 *         bold()
 *     }
 * }
 * ```
 *
 * Declaring a theme does not register it; call [register] explicitly during startup.
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
        styleDelegate(key = null, init = init)

    /**
     * Declares a semantic style property resolvable dynamically as [name] instead of the
     * property name.
     *
     * @throws IllegalArgumentException when [name] is blank or already declared by this theme.
     */
    protected fun style(
        name: String,
        init: StyleScope.() -> Unit,
    ): PropertyDelegateProvider<Theme, ReadOnlyProperty<Theme, Style>> = styleDelegate(key = name, init = init)

    private fun styleDelegate(
        key: String?,
        init: StyleScope.() -> Unit,
    ): PropertyDelegateProvider<Theme, ReadOnlyProperty<Theme, Style>> =
        PropertyDelegateProvider { theme, property ->
            val styleKey = key ?: property.name
            require(styleKey.isNotBlank()) { "Theme style name must not be blank." }
            val built = buildStyle(init)
            require(theme.styles.put(styleKey, built) == null) {
                "Duplicate style '$styleKey' in theme '${theme.name}'."
            }
            ReadOnlyProperty { _, _ -> built }
        }
}
