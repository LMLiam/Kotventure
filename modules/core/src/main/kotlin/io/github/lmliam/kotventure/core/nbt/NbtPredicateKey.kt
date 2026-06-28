package io.github.lmliam.kotventure.core.nbt

/**
 * A key reference within a compound predicate, used with [NbtPredicateScope.eq] to specify the expected value.
 */
public class NbtPredicateKey internal constructor(
    internal val name: String,
)
