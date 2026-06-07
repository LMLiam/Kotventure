package io.github.lmliam.kotventure.core.dsl

/**
 * Marks Kotventure builder scopes so nested DSL blocks cannot accidentally call outer receivers.
 */
@DslMarker
internal annotation class AdventureDsl
