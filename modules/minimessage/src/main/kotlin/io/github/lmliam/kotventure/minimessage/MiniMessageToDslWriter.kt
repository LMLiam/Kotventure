package io.github.lmliam.kotventure.minimessage

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.BlockNBTComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.EntityNBTComponent
import net.kyori.adventure.text.KeybindComponent
import net.kyori.adventure.text.NBTComponent
import net.kyori.adventure.text.ObjectComponent
import net.kyori.adventure.text.ScoreComponent
import net.kyori.adventure.text.SelectorComponent
import net.kyori.adventure.text.StorageNBTComponent
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.TranslationArgument
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.DataComponentValue
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.State

/**
 * Walks a MiniMessage-parsed component tree and emits the equivalent Kotventure component DSL source.
 *
 * The walker decides *what* to emit; [KotlinSourceBuilder] owns indentation and [MiniMessageToDslLiterals] renders leaf
 * values, so each function reads as the DSL it produces. Every payload MiniMessage can produce has a DSL form, with two
 * exceptions the parser can still reach — shadow colours and player-head object contents — which the DSL cannot yet
 * express; those are rejected at the point of emission rather than dropped silently.
 */
internal object MiniMessageToDslWriter {
    fun write(component: Component): String {
        if (component.isEmptyComponent()) {
            return "component {}"
        }

        return KotlinSourceBuilder()
            .apply { block("component") { appendRoot(component) } }
            .toString()
    }

    private fun Component.isEmptyComponent(): Boolean =
        this is TextComponent &&
                content().isEmpty() &&
                children().isEmpty() &&
                !hasDslOutput(style())
}

/** Emits [component], unwrapping a content-less, style-less text root into a bare sequence of its children. */
private fun KotlinSourceBuilder.appendRoot(component: Component) {
    if (component is TextComponent &&
        component.content().isEmpty() &&
        !hasDslOutput(component.style())
    ) {
        component.children().forEach { appendComponent(it) }
        return
    }

    appendComponent(component)
}

private val decorations: List<Pair<TextDecoration, String>> =
    listOf(
        TextDecoration.BOLD to "bold",
        TextDecoration.ITALIC to "italic",
        TextDecoration.UNDERLINED to "underlined",
        TextDecoration.STRIKETHROUGH to "strikethrough",
        TextDecoration.OBFUSCATED to "obfuscated",
    )

/**
 * Whether [style] carries anything that opens a component block. A shadow colour counts even though it has no DSL form:
 * including it routes the component through [appendStyle], which rejects it, instead of letting a block-less emission
 * drop it silently.
 */
private fun hasDslOutput(style: Style): Boolean =
    style.color() != null ||
        style.font() != null ||
        style.insertion() != null ||
        style.shadowColor() != null ||
        style.clickEvent() != null ||
        style.hoverEvent() != null ||
        decorations.any { (decoration) -> style.decoration(decoration) != State.NOT_SET }

/**
 * Dispatches to the emitter for [component]'s concrete type. Every emission is a self-contained call expression, so it
 * reads the same whether it appends a child inside a scope or stands alone as a translatable argument or separator.
 *
 * The branches cover every Adventure component type, but [Component] is an open interface rather than a sealed
 * hierarchy, so the compiler cannot prove exhaustiveness; the `else` guards against a future Adventure type instead of
 * dropping it silently.
 */
private fun KotlinSourceBuilder.appendComponent(component: Component) {
    when (component) {
        is TextComponent -> appendText(component)
        is TranslatableComponent -> appendTranslatable(component)
        is KeybindComponent -> appendKeybind(component)
        is ScoreComponent -> appendScore(component)
        is SelectorComponent -> appendSelector(component)
        is BlockNBTComponent -> appendBlockNbt(component)
        is EntityNBTComponent -> appendEntityNbt(component)
        is StorageNBTComponent -> appendStorageNbt(component)
        is ObjectComponent -> appendObject(component)
        else -> error("miniToDsl encountered an unsupported ${component::class.simpleName} component.")
    }
}

private fun KotlinSourceBuilder.appendText(component: TextComponent) {
    val text = component.content()
    val hasBlockBody = hasDslOutput(component.style()) || component.children().isNotEmpty()

    if (!hasBlockBody) {
        line("text(\"${escapeKotlinString(text)}\")")
        return
    }

    val header = if (text.isEmpty()) "text" else "text(\"${escapeKotlinString(text)}\")"
    block(header) {
        appendStyle(component.style())
        component.children().forEach { appendComponent(it) }
    }
}

private fun KotlinSourceBuilder.appendTranslatable(component: TranslatableComponent) {
    val fallback = component.fallback()
    val arguments = component.arguments()
    appendStructured(
        header = "translatable(\"${escapeKotlinString(component.key())}\")",
        component = component,
        hasExtraBody = fallback != null || arguments.isNotEmpty(),
    ) {
        fallback?.let { line("fallback(\"${escapeKotlinString(it)}\")") }
        arguments.forEach { appendArgument(it) }
    }
}

private fun KotlinSourceBuilder.appendKeybind(component: KeybindComponent) {
    appendStructured("keybind(\"${escapeKotlinString(component.keybind())}\")", component) {}
}

private fun KotlinSourceBuilder.appendScore(component: ScoreComponent) {
    val name = escapeKotlinString(component.name())
    val objective = escapeKotlinString(component.objective())
    appendStructured("score(\"$name\", \"$objective\")", component) {}
}

private fun KotlinSourceBuilder.appendSelector(component: SelectorComponent) {
    val separator = component.separator()
    appendStructured(
        header = "selector(\"${escapeKotlinString(component.pattern())}\")",
        component = component,
        hasExtraBody = separator != null,
    ) {
        separator?.let { appendComponentArgument("separator", it) }
    }
}

private fun KotlinSourceBuilder.appendBlockNbt(component: BlockNBTComponent) {
    appendNbt("blockNbt", "blockPos(\"${escapeKotlinString(component.pos().asString())}\")", component)
}

private fun KotlinSourceBuilder.appendEntityNbt(component: EntityNBTComponent) {
    appendNbt("entityNbt", "\"${escapeKotlinString(component.selector())}\"", component)
}

private fun KotlinSourceBuilder.appendStorageNbt(component: StorageNBTComponent) {
    appendNbt("storageNbt", keyLiteral(component.storage()), component)
}

/**
 * Emits an NBT component as `$function($source, "nbtPath")` — where [source] is the per-type first argument (a
 * `blockPos(...)`, a selector string, or a storage `key(...)`) — followed by the `interpret` flag and separator that
 * every NBT component can carry. Only an enabled [NBTComponent.interpret] flag is emitted, matching Adventure's default
 * of `false`.
 */
private fun KotlinSourceBuilder.appendNbt(
    function: String,
    source: String,
    component: NBTComponent<*>,
) {
    val interpret = component.interpret()
    val separator = component.separator()
    appendStructured(
        header = "$function($source, \"${escapeKotlinString(component.nbtPath())}\")",
        component = component,
        hasExtraBody = interpret || separator != null,
    ) {
        if (interpret) line("interpret(true)")
        separator?.let { appendComponentArgument("separator", it) }
    }
}

private fun KotlinSourceBuilder.appendObject(component: ObjectComponent) {
    val fallback = component.fallback()
    appendStructured(
        header = "display(${objectContentsLiteral(component.contents())})",
        component = component,
        hasExtraBody = fallback != null,
    ) {
        fallback?.let { appendComponentArgument("fallback", it) }
    }
}

/**
 * Emits [header] as a bare call when [component] carries no style, children, or [hasExtraBody], otherwise opens a block
 * and emits [body] (component-specific configuration such as fallback, arguments, or a separator) ahead of the shared
 * style and children.
 */
private fun KotlinSourceBuilder.appendStructured(
    header: String,
    component: Component,
    hasExtraBody: Boolean = false,
    body: KotlinSourceBuilder.() -> Unit,
) {
    val hasStyle = hasDslOutput(component.style())
    if (!hasExtraBody && !hasStyle && component.children().isEmpty()) {
        line(header)
        return
    }

    block(header) {
        body()
        appendStyle(component.style())
        component.children().forEach { appendComponent(it) }
    }
}

/**
 * Emits a translatable argument. Adventure wraps either a component or a primitive ([Boolean] / [Number]); the core
 * `translatable` DSL has an `arg(...)` overload for each, so every argument MiniMessage can produce round-trips.
 */
private fun KotlinSourceBuilder.appendArgument(argument: TranslationArgument) {
    when (val value = argument.value()) {
        is Component -> appendComponentArgument("arg", value)
        is Boolean -> line("arg($value)")
        is Number -> line("arg($value)")
        else -> error("miniToDsl encountered an unexpected ${value::class.simpleName} translatable argument.")
    }
}

/**
 * Emits `$label { ... }`, recursing through [appendRoot] for the wrapped component.
 *
 * The builder-block form (`arg { … }`, `separator { … }`) is used rather than a value argument because the per-type
 * builders (`text`, `translatable`, …) are members of
 * [io.github.lmliam.kotventure.core.component.ComponentScope] that return `Unit`, so an unwrapped `text("…")` could not
 * type-check as a [net.kyori.adventure.text.ComponentLike] value. Both [io.github.lmliam.kotventure.core.translatable]
 * `arg` and [io.github.lmliam.kotventure.core.selector] `separator` expose a `TextScope` block overload for exactly this.
 */
private fun KotlinSourceBuilder.appendComponentArgument(
    label: String,
    component: Component,
) {
    block(label) { appendRoot(component) }
}

private fun KotlinSourceBuilder.appendStyle(style: Style) {
    require(style.shadowColor() == null) {
        "miniToDsl cannot represent shadow colours: the component DSL has no shadow-colour surface."
    }

    style.color()?.let { color -> line("color(${colorLiteral(color)})") }

    decorations.forEach { (decoration, functionName) ->
        if (style.decoration(decoration) == State.TRUE) {
            line("$functionName()")
        }
    }

    appendStyleBlock(style)

    style.clickEvent()?.let { event -> appendClickEvent(event) }
    style.hoverEvent()?.let { event -> appendHoverEvent(event) }
}

/**
 * Emits a `style { ... }` block for the attributes that have no shorthand directly on a component scope — the font, the
 * insertion text, and any decoration explicitly disabled to override an inherited style — or nothing when the style
 * carries none of them.
 */
private fun KotlinSourceBuilder.appendStyleBlock(style: Style) {
    val font = style.font()
    val insertion = style.insertion()
    val disabledDecorations =
        decorations.filter { (decoration) -> style.decoration(decoration) == State.FALSE }

    if (font == null && insertion == null && disabledDecorations.isEmpty()) {
        return
    }

    block("style") {
        font?.let { line("font(${keyLiteral(it)})") }
        insertion?.let { line("insertion(\"${escapeKotlinString(it)}\")") }
        disabledDecorations.forEach { (_, functionName) -> line("$functionName(false)") }
    }
}

private fun KotlinSourceBuilder.appendClickEvent(event: ClickEvent<*>) {
    block("click") {
        when (event.action()) {
            ClickEvent.Action.OPEN_URL -> line("openUrl(\"${escapeKotlinString(event.textPayload())}\")")
            ClickEvent.Action.OPEN_FILE -> line("openFile(\"${escapeKotlinString(event.textPayload())}\")")
            ClickEvent.Action.RUN_COMMAND -> line("run(\"${escapeKotlinString(event.textPayload())}\")")
            ClickEvent.Action.SUGGEST_COMMAND -> line("suggest(\"${escapeKotlinString(event.textPayload())}\")")
            ClickEvent.Action.CHANGE_PAGE -> line("changePage(${event.intPayload()})")
            ClickEvent.Action.COPY_TO_CLIPBOARD -> line("copy(\"${escapeKotlinString(event.textPayload())}\")")
            else -> line("// callback not representable")
        }
    }
}

private fun ClickEvent<*>.textPayload(): String = (payload() as ClickEvent.Payload.Text).value()

private fun ClickEvent<*>.intPayload(): Int = (payload() as ClickEvent.Payload.Int).integer()

private fun KotlinSourceBuilder.appendHoverEvent(event: HoverEvent<*>) {
    block("hover") {
        when (event.action()) {
            HoverEvent.Action.SHOW_TEXT -> block("text") { appendRoot(event.value() as Component) }
            HoverEvent.Action.SHOW_ITEM -> appendShowItem(event.value() as HoverEvent.ShowItem)
            HoverEvent.Action.SHOW_ENTITY -> appendShowEntity(event.value() as HoverEvent.ShowEntity)
            else -> throw IllegalArgumentException(
                "miniToDsl cannot represent the ${event.action().name()} hover action.",
            )
        }
    }
}

private fun KotlinSourceBuilder.appendShowItem(item: HoverEvent.ShowItem) {
    val arguments =
        buildList<() -> Unit> {
            add { line("key = ${keyLiteral(item.item())}") }
            if (item.count() != 1) add { line("count = ${item.count()}") }
            if (item.dataComponents().isNotEmpty()) add { appendDataComponents(item.dataComponents()) }
        }

    if (arguments.size == 1) {
        line("item(${keyLiteral(item.item())})")
        return
    }

    openArguments("item(", arguments)
    line(")")
}

private fun KotlinSourceBuilder.appendDataComponents(dataComponents: Map<Key, DataComponentValue>) {
    val entries: List<() -> Unit> =
        dataComponents.entries
            .sortedBy { (key, _) -> key.asString() }
            .map { (key, value) ->
                { line("${keyLiteral(key)} to ${dataComponentValueLiteral(value)}") }
            }

    openArguments("dataComponents = mapOf(", entries)
    line(")")
}

private fun KotlinSourceBuilder.appendShowEntity(entity: HoverEvent.ShowEntity) {
    val arguments: List<() -> Unit> =
        listOf(
            { line("type = ${keyLiteral(entity.type())}") },
            { line("id = UUID.fromString(\"${entity.id()}\")") },
        )

    openArguments("entity(", arguments)

    val name = entity.name()
    if (name == null) {
        line(")")
    } else {
        block(")") { appendRoot(name) }
    }
}
