package io.github.lmliam.kotventure.core.selector

import java.math.BigDecimal

internal fun formatSelectorNumber(value: Double): String =
    BigDecimal
        .valueOf(value)
        .stripTrailingZeros()
        .toPlainString()
