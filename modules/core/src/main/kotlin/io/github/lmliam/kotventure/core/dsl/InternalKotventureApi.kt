package io.github.lmliam.kotventure.core.dsl

/**
 * Marks infrastructure shared between Kotventure modules but not intended as end-user API.
 *
 * Opted-in declarations may change without the compatibility guarantees of the public DSL.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This is internal Kotventure infrastructure and is not intended for end-user code.",
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPEALIAS,
)
@MustBeDocumented
public annotation class InternalKotventureApi
