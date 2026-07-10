package io.github.lmliam.kotventure.core.objectcomponent

import net.kyori.adventure.text.Component

/**
 * Renders object components to their fallback components where a fallback is configured.
 */
public fun Component.renderObjectFallbacks(): Component = ObjectFallbackRenderer.render(this, Unit)
