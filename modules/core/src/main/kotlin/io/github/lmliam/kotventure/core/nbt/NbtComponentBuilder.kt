package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.core.component.ComponentBuilder
import io.github.lmliam.kotventure.core.text.TextScope
import io.github.lmliam.kotventure.core.text.buildTextComponent
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.NBTComponent
import net.kyori.adventure.text.NBTComponentBuilder

internal class NbtComponentBuilder<C : NBTComponent<C>, B : NBTComponentBuilder<C, B>>(
    builder: B,
) : ComponentBuilder<C, B>(builder),
    NbtScope {
    override fun interpret(interpret: Boolean) {
        singleAssignments.assign("interpret")
        builder.interpret(interpret)
    }

    override fun separator(separator: ComponentLike) {
        singleAssignments.assign("separator")
        builder.separator(separator)
    }

    override fun separator(init: TextScope.() -> Unit) {
        separator(buildTextComponent(init))
    }
}
