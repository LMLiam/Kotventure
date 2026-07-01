package io.github.lmliam.kotventure.core.selector

/**
 * A negated selector argument value, rendering with vanilla's `!` prefix.
 *
 * Produced by the `!` operators scoped inside the selector DSL — `tag(!"muted")`,
 * `gamemode(!survival)`, `type(!"zombie")` — and consumed by the negated argument overloads. Each
 * scope offers `!` overloads only for the arguments its selector head may negate.
 *
 * @sample io.github.lmliam.kotventure.core.selector.negatedCommonArgumentsSample
 */
public class Excluded<T> internal constructor(
    internal val value: T,
)
