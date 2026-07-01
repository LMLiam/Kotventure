package io.github.lmliam.kotventure.core.selector

/**
 * A vanilla coordinate argument. Enum declaration order is the canonical rendering order.
 */
internal sealed interface SelectorAxis {
    val argument: String
}

internal enum class OriginAxis(
    override val argument: String,
) : SelectorAxis {
    X("x"),
    Y("y"),
    Z("z"),
}

internal enum class VolumeAxis(
    override val argument: String,
) : SelectorAxis {
    DX("dx"),
    DY("dy"),
    DZ("dz"),
}
