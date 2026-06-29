package io.github.lmliam.kotventure.core.text

internal fun joinArraySample() {
    val list = arrayOf(text("a"), text("b"), text("c")).join { separator(text(", ")) }
}

internal fun joinIterableSample() {
    val list = listOf(text("a"), text("b")).join { separator(text(", ")) }
}
