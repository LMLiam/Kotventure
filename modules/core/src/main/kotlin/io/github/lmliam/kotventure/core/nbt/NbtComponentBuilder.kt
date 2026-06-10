package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.core.component.ComponentScopeBuilder
import io.github.lmliam.kotventure.core.text.TextComponentBuilder
import io.github.lmliam.kotventure.core.text.TextScope
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.NBTComponent
import net.kyori.adventure.text.NBTComponentBuilder

internal abstract class NbtComponentBuilder<C, B>(
    builder: B,
) : ComponentScopeBuilder<C, B>(builder),
    NbtScope
    where C : NBTComponent<C>,
          B : NBTComponentBuilder<C, B> {
    override fun interpret(interpret: Boolean) {
        builder.interpret(interpret)
    }

    override fun separator(separator: ComponentLike) {
        builder.separator(separator)
    }

    override fun separator(init: TextScope.() -> Unit) {
        builder.separator(TextComponentBuilder().apply(init).build())
    }
}
