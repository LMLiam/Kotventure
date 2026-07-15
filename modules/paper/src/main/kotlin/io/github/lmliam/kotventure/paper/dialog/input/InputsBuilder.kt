package io.github.lmliam.kotventure.paper.dialog.input

import io.papermc.paper.registry.data.dialog.input.DialogInput

internal class InputsBuilder : InputsScope {
    private val inputs = mutableListOf<DialogInput>()

    override fun text(
        key: String,
        init: TextInputScope.() -> Unit,
    ) {
        inputs += TextInputBuilder(key).apply(init).build()
    }

    override fun boolean(
        key: String,
        init: BooleanInputScope.() -> Unit,
    ) {
        inputs += BooleanInputBuilder(key).apply(init).build()
    }

    override fun range(
        key: String,
        range: ClosedFloatingPointRange<Float>,
        init: NumberRangeInputScope.() -> Unit,
    ) {
        inputs += NumberRangeInputBuilder(key, range).apply(init).build()
    }

    override fun option(
        key: String,
        init: SingleOptionInputScope.() -> Unit,
    ) {
        inputs += SingleOptionInputBuilder(key).apply(init).build()
    }

    internal fun build(): List<DialogInput> = inputs.toList()
}
