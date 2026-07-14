package io.github.lmliam.kotventure.core.dsl

/**
 * Returns a delegate that additionally rejects values outside [range].
 *
 * @throws IllegalArgumentException when an assigned value is outside [range].
 */
@InternalKotventureApi
public fun <T : Comparable<T>> OnceAssign.inRange(range: ClosedRange<T>): ValidatedOnceAssign<T> =
    ValidatedOnceAssign(alreadySetMessage) { name, value ->
        require(value in range) { "'$name' must be in $range, was $value." }
    }

/**
 * Returns a delegate that additionally rejects non-positive values.
 *
 * @throws IllegalArgumentException when an assigned value is not positive.
 */
@InternalKotventureApi
public fun <T> OnceAssign.positive(): ValidatedOnceAssign<T> where T : Number, T : Comparable<T> =
    ValidatedOnceAssign(alreadySetMessage) { name, value ->
        require(value.toDouble() > 0.0) { "'$name' must be positive, was $value." }
    }
