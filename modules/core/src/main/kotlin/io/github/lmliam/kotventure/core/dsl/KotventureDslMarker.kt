package io.github.lmliam.kotventure.core.dsl

/**
 * Marks Kotventure DSL scopes so nested blocks cannot accidentally call outer receivers.
 */
@DslMarker
internal annotation class KotventureDslMarker
