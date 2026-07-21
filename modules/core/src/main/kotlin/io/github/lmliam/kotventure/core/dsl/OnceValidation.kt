package io.github.lmliam.kotventure.core.dsl

/**
 * Adds [range] validation to this write-once delegate.
 *
 * Validation occurs when the property receives a non-null value. A rejected value does not consume the property, so
 * a later valid assignment can succeed. A `null` assignment bypasses validation and consumes the property.
 *
 * @throws IllegalArgumentException when an assigned value is outside [range].
 */
@InternalKotventureApi
public fun <T : Comparable<T>> OnceAssign.inRange(range: ClosedRange<T>): ValidatedOnceAssign<T> =
    ValidatedOnceAssign(alreadySetMessage) { name, value ->
        require(value in range) { "'$name' must be in $range, was $value." }
    }

/**
 * Adds positive-value validation to this write-once delegate.
 *
 * Validation occurs when the property receives a non-null value. A rejected value does not consume the property, so
 * a later valid assignment can succeed. A `null` assignment bypasses validation and consumes the property.
 *
 * @throws IllegalArgumentException when an assigned value is not positive.
 */
@InternalKotventureApi
public fun <T> OnceAssign.positive(): ValidatedOnceAssign<T> where T : Number, T : Comparable<T> =
    ValidatedOnceAssign(alreadySetMessage) { name, value ->
        require(value.toDouble() > 0.0) { "'$name' must be positive, was $value." }
    }
