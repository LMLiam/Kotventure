package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.core.component.ComponentBuilder
import io.github.lmliam.kotventure.core.dsl.once
import io.github.lmliam.kotventure.core.text.TextScope
import io.github.lmliam.kotventure.core.text.buildTextComponent
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.NBTComponent
import net.kyori.adventure.text.NBTComponentBuilder

internal class NbtComponentBuilder<C : NBTComponent<C>, B : NBTComponentBuilder<C, B>>(
    builder: B,
) : ComponentBuilder<C, B>(builder),
    NbtScope {
    private var interpret: Boolean? by once()
    private var separator: ComponentLike? by once()

    override fun interpret(interpret: Boolean) {
        this.interpret = interpret
        builder.interpret(interpret)
    }

    override fun separator(separator: ComponentLike) {
        this.separator = separator
        builder.separator(separator)
    }

    override fun separator(init: TextScope.() -> Unit) {
        separator(buildTextComponent(init))
    }
}
