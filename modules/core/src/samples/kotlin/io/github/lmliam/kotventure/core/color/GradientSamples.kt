package io.github.lmliam.kotventure.core.color

internal fun gradientSample() {
    val fire = gradient(hex("#FF0000"), hex("#FFAA00"), hex("#FFFF00"))
}

internal fun gradientTextSample() {
    val title = gradientText("Kotventure", hex("#FF0000"), hex("#0000FF"))
}
