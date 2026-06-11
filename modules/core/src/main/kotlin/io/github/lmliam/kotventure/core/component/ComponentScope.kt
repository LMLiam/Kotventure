package io.github.lmliam.kotventure.core.component

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.keybind.KeybindScope
import io.github.lmliam.kotventure.core.nbt.BlockNbtScope
import io.github.lmliam.kotventure.core.nbt.EntityNbtScope
import io.github.lmliam.kotventure.core.nbt.StorageNbtScope
import io.github.lmliam.kotventure.core.objectcomponent.ObjectScope
import io.github.lmliam.kotventure.core.score.ScoreScope
import io.github.lmliam.kotventure.core.selector.SelectorScope
import io.github.lmliam.kotventure.core.style.StyleScope
import io.github.lmliam.kotventure.core.text.TextScope
import io.github.lmliam.kotventure.core.translatable.TranslatableScope
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.BlockNBTComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.`object`.ObjectContents
import java.time.temporal.TemporalAmount
import io.github.lmliam.kotventure.core.event.callback as callbackEvent
import io.github.lmliam.kotventure.core.event.changePage as changePageEvent
import io.github.lmliam.kotventure.core.event.copy as copyEvent
import io.github.lmliam.kotventure.core.event.copyToClipboard as copyToClipboardEvent
import io.github.lmliam.kotventure.core.event.open as openEvent
import io.github.lmliam.kotventure.core.event.openFile as openFileEvent
import io.github.lmliam.kotventure.core.event.openUrl as openUrlEvent
import io.github.lmliam.kotventure.core.event.runCommand as runCommandEvent
import io.github.lmliam.kotventure.core.event.suggestCommand as suggestCommandEvent
import kotlin.time.Duration as KotlinDuration

/**
 * Scope for configuring behavior shared by every Adventure component builder.
 */
@KotventureDslMarker
public interface ComponentScope {
    /**
     * Applies a text color to the component being configured.
     */
    public fun color(color: TextColor)

    /**
     * Applies a complete Adventure style to the component being configured.
     */
    public fun style(style: Style)

    /**
     * Applies style attributes from [init] to the component being configured.
     */
    public fun style(init: StyleScope.() -> Unit)

    /**
     * Applies [event] as the click event, or clears the click event when [event] is null.
     */
    public fun clickEvent(event: ClickEvent<*>?)

    /**
     * Applies a click event that opens [url].
     */
    public fun open(url: String) {
        clickEvent(openEvent(url))
    }

    /**
     * Applies a click event that opens [url].
     */
    public fun openUrl(url: String) {
        clickEvent(openUrlEvent(url))
    }

    /**
     * Applies a click event that opens a local [file] path.
     */
    public fun openFile(file: String) {
        clickEvent(openFileEvent(file))
    }

    /**
     * Applies a click event that runs [command].
     */
    public fun runCommand(command: String) {
        clickEvent(runCommandEvent(command))
    }

    /**
     * Applies a click event that suggests [command] in chat.
     */
    public fun suggestCommand(command: String) {
        clickEvent(suggestCommandEvent(command))
    }

    /**
     * Applies a click event that changes a book to [page].
     */
    public fun changePage(page: Int) {
        clickEvent(changePageEvent(page))
    }

    /**
     * Applies a click event that copies [text] to the clipboard.
     */
    public fun copy(text: String) {
        clickEvent(copyEvent(text))
    }

    /**
     * Applies a click event that copies [text] to the clipboard.
     */
    public fun copyToClipboard(text: String) {
        clickEvent(copyToClipboardEvent(text))
    }

    /**
     * Applies a server-side callback click event from [function].
     */
    public fun callback(function: ClickCallback<Audience>) {
        clickEvent(callbackEvent(function))
    }

    /**
     * Applies a server-side callback click event from [function] with [uses] and [lifetime].
     */
    public fun callback(
        uses: Int,
        lifetime: TemporalAmount,
        function: ClickCallback<Audience>,
    ) {
        clickEvent(callbackEvent(uses, lifetime, function))
    }

    /**
     * Applies a server-side callback click event from [function] with [uses] and [lifetime].
     */
    public fun callback(
        uses: Int,
        lifetime: KotlinDuration,
        function: ClickCallback<Audience>,
    ) {
        clickEvent(callbackEvent(uses, lifetime, function))
    }

    /**
     * Applies a server-side callback click event from [function] with prebuilt [options].
     */
    public fun callback(
        options: ClickCallback.Options,
        function: ClickCallback<Audience>,
    ) {
        clickEvent(callbackEvent(options, function))
    }

    /**
     * Enables [decoration] on the component being configured.
     */
    public fun decorate(decoration: TextDecoration)

    /**
     * Enables bold text on the component being configured.
     */
    public fun bold()

    /**
     * Enables italic text on the component being configured.
     */
    public fun italic()

    /**
     * Enables underlined text on the component being configured.
     */
    public fun underlined()

    /**
     * Enables strikethrough text on the component being configured.
     */
    public fun strikethrough()

    /**
     * Enables obfuscated text on the component being configured.
     */
    public fun obfuscated()

    /**
     * Appends an existing Adventure [Component] as a child of the component being configured.
     */
    public fun append(component: Component)

    /**
     * Appends an Adventure newline component as a child of the component being configured.
     */
    public fun newline()

    /**
     * Appends a nested text child with [value] as its initial content and configured by [init].
     */
    public fun text(
        value: String,
        init: TextScope.() -> Unit = {},
    )

    /**
     * Appends a nested text child configured by [init].
     */
    public fun text(init: TextScope.() -> Unit)

    /**
     * Appends a nested translatable child with [key] as its translation key.
     */
    public fun translatable(
        key: String,
        init: TranslatableScope.() -> Unit = {},
    )

    /**
     * Appends a nested keybind child with [keybind] as its keybind identifier.
     */
    public fun keybind(
        keybind: String,
        init: KeybindScope.() -> Unit = {},
    )

    /**
     * Appends a nested score child with [name] and [objective].
     */
    public fun score(
        name: String,
        objective: String,
        init: ScoreScope.() -> Unit = {},
    )

    /**
     * Appends a nested selector child with [pattern] as its selector pattern.
     */
    public fun selector(
        pattern: String,
        init: SelectorScope.() -> Unit = {},
    )

    /**
     * Appends a nested block NBT child with [pos] and [nbtPath].
     */
    public fun blockNbt(
        pos: BlockNBTComponent.Pos,
        nbtPath: String,
        init: BlockNbtScope.() -> Unit = {},
    )

    /**
     * Appends a nested entity NBT child with [selector] and [nbtPath].
     */
    public fun entityNbt(
        selector: String,
        nbtPath: String,
        init: EntityNbtScope.() -> Unit = {},
    )

    /**
     * Appends a nested storage NBT child with [storage] and [nbtPath].
     */
    public fun storageNbt(
        storage: Key,
        nbtPath: String,
        init: StorageNbtScope.() -> Unit = {},
    )

    /**
     * Appends a nested object child with [contents].
     */
    public fun display(
        contents: ObjectContents,
        init: ObjectScope.() -> Unit = {},
    )
}
