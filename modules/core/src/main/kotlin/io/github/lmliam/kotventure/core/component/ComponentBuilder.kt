package io.github.lmliam.kotventure.core.component

import io.github.lmliam.kotventure.core.keybind.KeybindScope
import io.github.lmliam.kotventure.core.nbt.BlockNbtScope
import io.github.lmliam.kotventure.core.nbt.EntityNbtScope
import io.github.lmliam.kotventure.core.nbt.StorageNbtScope
import io.github.lmliam.kotventure.core.objectcomponent.ObjectScope
import io.github.lmliam.kotventure.core.score.ScoreScope
import io.github.lmliam.kotventure.core.selector.SelectorScope
import io.github.lmliam.kotventure.core.style.StyleBuilder
import io.github.lmliam.kotventure.core.style.StyleScope
import io.github.lmliam.kotventure.core.text.TextScope
import io.github.lmliam.kotventure.core.translatable.TranslatableScope
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.BlockNBTComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEventSource
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.`object`.ObjectContents
import io.github.lmliam.kotventure.core.keybind.keybind as keybindComponent
import io.github.lmliam.kotventure.core.nbt.blockNbt as blockNbtComponent
import io.github.lmliam.kotventure.core.nbt.entityNbt as entityNbtComponent
import io.github.lmliam.kotventure.core.nbt.storageNbt as storageNbtComponent
import io.github.lmliam.kotventure.core.objectcomponent.display as displayComponent
import io.github.lmliam.kotventure.core.score.score as scoreComponent
import io.github.lmliam.kotventure.core.selector.selector as selectorComponent
import io.github.lmliam.kotventure.core.text.text as textComponent
import io.github.lmliam.kotventure.core.translatable.translatable as translatableComponent
import net.kyori.adventure.text.ComponentBuilder as AdventureComponentBuilder

internal abstract class ComponentBuilder<C : Component, B : AdventureComponentBuilder<C, B>>(
    protected val builder: B,
) : ComponentScope {
    override fun color(color: TextColor) {
        builder.color(color)
    }

    override fun style(style: Style) {
        builder.style(style)
    }

    override fun style(init: StyleScope.() -> Unit) {
        builder.style { styleBuilder -> StyleBuilder(styleBuilder).init() }
    }

    override fun click(event: ClickEvent<*>?) {
        builder.clickEvent(event)
    }

    override fun hover(source: HoverEventSource<*>?) {
        builder.hoverEvent(source)
    }

    override fun decorate(decoration: TextDecoration) {
        builder.decoration(decoration, true)
    }

    override fun bold() {
        decorate(TextDecoration.BOLD)
    }

    override fun italic() {
        decorate(TextDecoration.ITALIC)
    }

    override fun underlined() {
        decorate(TextDecoration.UNDERLINED)
    }

    override fun strikethrough() {
        decorate(TextDecoration.STRIKETHROUGH)
    }

    override fun obfuscated() {
        decorate(TextDecoration.OBFUSCATED)
    }

    override fun append(component: Component) {
        builder.append(component)
    }

    override fun newline() {
        builder.append(Component.newline())
    }

    override fun text(
        value: String,
        init: TextScope.() -> Unit,
    ) {
        text {
            content(value)
            init()
        }
    }

    override fun text(init: TextScope.() -> Unit) {
        append(textComponent(init))
    }

    override fun translatable(
        key: String,
        init: TranslatableScope.() -> Unit,
    ) {
        append(translatableComponent(key, init))
    }

    override fun keybind(
        keybind: String,
        init: KeybindScope.() -> Unit,
    ) {
        append(keybindComponent(keybind, init))
    }

    override fun score(
        name: String,
        objective: String,
        init: ScoreScope.() -> Unit,
    ) {
        append(scoreComponent(name, objective, init))
    }

    override fun selector(
        pattern: String,
        init: SelectorScope.() -> Unit,
    ) {
        append(selectorComponent(pattern, init))
    }

    override fun blockNbt(
        pos: BlockNBTComponent.Pos,
        nbtPath: String,
        init: BlockNbtScope.() -> Unit,
    ) {
        append(blockNbtComponent(pos, nbtPath, init))
    }

    override fun entityNbt(
        selector: String,
        nbtPath: String,
        init: EntityNbtScope.() -> Unit,
    ) {
        append(entityNbtComponent(selector, nbtPath, init))
    }

    override fun storageNbt(
        storage: Key,
        nbtPath: String,
        init: StorageNbtScope.() -> Unit,
    ) {
        append(storageNbtComponent(storage, nbtPath, init))
    }

    override fun display(
        contents: ObjectContents,
        init: ObjectScope.() -> Unit,
    ) {
        append(displayComponent(contents, init))
    }

    internal open fun build(): Component = builder.build()
}
