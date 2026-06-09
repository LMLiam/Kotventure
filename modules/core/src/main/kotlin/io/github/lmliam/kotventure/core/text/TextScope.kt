package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * Scope for configuring text-specific content on a text component.
 */
@KotventureDslMarker
public interface TextScope : ComponentScope {
    /**
     * Replaces the text content of the component being configured.
     */
    public fun content(value: String)
}
