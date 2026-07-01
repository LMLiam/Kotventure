package io.github.lmliam.kotventure.core.selector

import java.math.BigDecimal

/**
 * Renders a finite double in vanilla selector syntax, which accepts no exponent notation:
 * `1e20` becomes `100000000000000000000`, `10.0` becomes `10`.
 */
internal fun formatSelectorNumber(value: Double): String =
    BigDecimal
        .valueOf(value)
        .stripTrailingZeros()
        .toPlainString()
