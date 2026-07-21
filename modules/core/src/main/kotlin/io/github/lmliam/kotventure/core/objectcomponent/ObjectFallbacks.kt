package io.github.lmliam.kotventure.core.objectcomponent

import net.kyori.adventure.text.Component

/**
 * Replaces each object component that has a fallback with its rendered fallback.
 *
 * The renderer processes the complete component tree. It keeps object components that have no fallback. For a
 * replacement, it keeps the fallback's style and children, and then appends the original object's rendered children.
 * This function returns a new component tree and does not change [this].
 */
public fun Component.renderObjectFallbacks(): Component = ObjectFallbackRenderer.render(this, Unit)
