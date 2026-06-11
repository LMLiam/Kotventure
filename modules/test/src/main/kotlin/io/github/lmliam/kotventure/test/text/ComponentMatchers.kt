package io.github.lmliam.kotventure.test.text

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
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
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.State
import net.kyori.adventure.text.`object`.ObjectContents

/**
 * Asserts that this component tree contains [expected] in its text content.
 */
public infix fun Component.shouldContainText(expected: String): Component =
    apply {
        this should haveTextContent(expected)
    }

/**
 * Asserts that this component has [expected] as its root color.
 */
public infix fun Component.shouldHaveColor(expected: TextColor): Component =
    apply {
        this should haveColor(expected)
    }

/**
 * Asserts that this component has no root color.
 */
public fun Component.shouldNotHaveColor(): Component =
    apply {
        this should haveNoColor()
    }

/**
 * Asserts that this component has exactly [expected] as its root Adventure style.
 */
public infix fun Component.shouldHaveStyle(expected: Style): Component =
    apply {
        this should haveStyle(expected)
    }

/**
 * Asserts that this component has [expected] enabled on its root style.
 */
public infix fun Component.shouldHaveDecoration(expected: TextDecoration): Component =
    apply {
        this should haveDecoration(expected)
    }

/**
 * Asserts that this component has [decoration] set to [state] on its root style.
 */
public fun Component.shouldHaveDecoration(
    decoration: TextDecoration,
    state: State,
): Component =
    apply {
        this should haveDecorationState(decoration, state)
    }

/**
 * Asserts that this component has no explicit [expected] state on its root style.
 */
public infix fun Component.shouldNotHaveDecoration(expected: TextDecoration): Component =
    apply {
        this should haveDecorationState(expected, State.NOT_SET)
    }

/**
 * Asserts that this component has [expected] as its root font.
 */
public infix fun Component.shouldHaveFont(expected: Key): Component =
    apply {
        this should haveFont(expected)
    }

/**
 * Asserts that this component has no root font.
 */
public fun Component.shouldNotHaveFont(): Component =
    apply {
        this should haveNoFont()
    }

/**
 * Asserts that this component has [expected] as its root shift-click insertion text.
 */
public infix fun Component.shouldHaveInsertion(expected: String): Component =
    apply {
        this should haveInsertion(expected)
    }

/**
 * Asserts that this component has no root shift-click insertion text.
 */
public fun Component.shouldNotHaveInsertion(): Component =
    apply {
        this should haveNoInsertion()
    }

/**
 * Asserts that this component has exactly [expected] as its root click event.
 */
public infix fun Component.shouldHaveClickEvent(expected: ClickEvent<*>): Component =
    apply {
        this should haveClickEvent(expected)
    }

/**
 * Asserts that this component has [expected] as its root click event action.
 */
public infix fun Component.shouldHaveClickAction(expected: ClickEvent.Action<*>): Component =
    apply {
        this should haveClickAction(expected)
    }

/**
 * Asserts that this component has [expected] as its root click event text payload.
 */
public infix fun Component.shouldHaveClickTextPayload(expected: String): Component =
    apply {
        this should haveClickTextPayload(expected)
    }

/**
 * Asserts that this component has [expected] as its root click event integer payload.
 */
public infix fun Component.shouldHaveClickIntPayload(expected: Int): Component =
    apply {
        this should haveClickIntPayload(expected)
    }

/**
 * Asserts that this component has no root click event.
 */
public fun Component.shouldNotHaveClickEvent(): Component =
    apply {
        this should haveNoClickEvent()
    }

/**
 * Asserts that this component has exactly [expected] direct child components.
 */
public infix fun Component.shouldHaveChildCount(expected: Int): Component =
    apply {
        this should haveChildCount(expected)
    }

/**
 * Asserts that this component is translatable and has [expected] as its translation key.
 */
public infix fun Component.shouldHaveTranslationKey(expected: String): Component =
    apply {
        this should haveTranslationKey(expected)
    }

/**
 * Asserts that this component is translatable and has [expected] as its fallback text.
 */
public infix fun Component.shouldHaveFallback(expected: String): Component =
    apply {
        this should haveFallback(expected)
    }

/**
 * Asserts that this component is translatable and has no fallback text.
 */
public fun Component.shouldNotHaveFallback(): Component =
    apply {
        this should haveNoFallback()
    }

/**
 * Asserts that this component is translatable and has exactly [expected] translation arguments.
 */
public infix fun Component.shouldHaveArgumentCount(expected: Int): Component =
    apply {
        this should haveArgumentCount(expected)
    }

/**
 * Asserts that this component is translatable and has exactly [expected] translation arguments in order.
 */
public fun Component.shouldHaveArguments(vararg expected: TranslationArgument): Component =
    apply {
        this should haveArguments(expected.toList())
    }

/**
 * Asserts that this component is a [KeybindComponent] and returns it typed.
 */
public fun Component.shouldBeKeybindComponent(): KeybindComponent = shouldBeComponentType("keybind")

/**
 * Asserts that this keybind component has [expected] as its keybind.
 */
public infix fun KeybindComponent.shouldHaveKeybind(expected: String): KeybindComponent =
    apply {
        this should haveKeybind(expected)
    }

/**
 * Asserts that this component is a [ScoreComponent] and returns it typed.
 */
public fun Component.shouldBeScoreComponent(): ScoreComponent = shouldBeComponentType("score")

/**
 * Asserts that this score component has [expected] as its score name.
 */
public infix fun ScoreComponent.shouldHaveScoreName(expected: String): ScoreComponent =
    apply {
        this should haveScoreName(expected)
    }

/**
 * Asserts that this score component has [expected] as its score objective.
 */
public infix fun ScoreComponent.shouldHaveScoreObjective(expected: String): ScoreComponent =
    apply {
        this should haveScoreObjective(expected)
    }

/**
 * Asserts that this component is a [SelectorComponent] and returns it typed.
 */
public fun Component.shouldBeSelectorComponent(): SelectorComponent = shouldBeComponentType("selector")

/**
 * Asserts that this selector component has [expected] as its selector pattern.
 */
public infix fun SelectorComponent.shouldHaveSelectorPattern(expected: String): SelectorComponent =
    apply {
        this should haveSelectorPattern(expected)
    }

/**
 * Asserts that this selector component has [expected] as its separator.
 */
public infix fun SelectorComponent.shouldHaveSelectorSeparator(expected: Component): SelectorComponent =
    apply {
        this should haveSelectorSeparator(expected)
    }

/**
 * Asserts that this selector component has no separator.
 */
public fun SelectorComponent.shouldNotHaveSelectorSeparator(): SelectorComponent =
    apply {
        this should haveNoSelectorSeparator()
    }

/**
 * Asserts that this component is an [ObjectComponent] and returns it typed.
 */
public fun Component.shouldBeObjectComponent(): ObjectComponent = shouldBeComponentType("object")

/**
 * Asserts that this object component has [expected] as its contents.
 */
public infix fun ObjectComponent.shouldHaveObjectContents(expected: ObjectContents): ObjectComponent =
    apply {
        this should haveObjectContents(expected)
    }

/**
 * Asserts that this object component has [expected] as its fallback component.
 */
public infix fun ObjectComponent.shouldHaveObjectFallback(expected: Component): ObjectComponent =
    apply {
        this should haveObjectFallback(expected)
    }

/**
 * Asserts that this object component has no fallback component.
 */
public fun ObjectComponent.shouldNotHaveObjectFallback(): ObjectComponent =
    apply {
        this should haveNoObjectFallback()
    }

/**
 * Asserts that this component is a [BlockNBTComponent] and returns it typed.
 */
public fun Component.shouldBeBlockNbtComponent(): BlockNBTComponent = shouldBeComponentType("block NBT")

/**
 * Asserts that this component is an [EntityNBTComponent] and returns it typed.
 */
public fun Component.shouldBeEntityNbtComponent(): EntityNBTComponent = shouldBeComponentType("entity NBT")

/**
 * Asserts that this component is a [StorageNBTComponent] and returns it typed.
 */
public fun Component.shouldBeStorageNbtComponent(): StorageNBTComponent = shouldBeComponentType("storage NBT")

/**
 * Asserts that this NBT component has [expected] as its NBT path.
 */
public infix fun NBTComponent<*>.shouldHaveNbtPath(expected: String): NBTComponent<*> =
    apply {
        this should haveNbtPath(expected)
    }

/**
 * Asserts that this NBT component interprets fetched NBT as component JSON.
 */
public fun NBTComponent<*>.shouldInterpret(): NBTComponent<*> =
    apply {
        this should haveInterpretState(true)
    }

/**
 * Asserts that this NBT component does not interpret fetched NBT as component JSON.
 */
public fun NBTComponent<*>.shouldNotInterpret(): NBTComponent<*> =
    apply {
        this should haveInterpretState(false)
    }

/**
 * Asserts that this NBT component has [expected] as its separator.
 */
public infix fun NBTComponent<*>.shouldHaveNbtSeparator(expected: Component): NBTComponent<*> =
    apply {
        this should haveNbtSeparator(expected)
    }

/**
 * Asserts that this NBT component has no separator.
 */
public fun NBTComponent<*>.shouldNotHaveNbtSeparator(): NBTComponent<*> =
    apply {
        this should haveNoNbtSeparator()
    }

/**
 * Asserts that this block NBT component has [expected] as its block position.
 */
public infix fun BlockNBTComponent.shouldHaveBlockPos(expected: BlockNBTComponent.Pos): BlockNBTComponent =
    apply {
        this should haveBlockPos(expected)
    }

/**
 * Asserts that this entity NBT component has [expected] as its selector.
 */
public infix fun EntityNBTComponent.shouldHaveEntitySelector(expected: String): EntityNBTComponent =
    apply {
        this should haveEntitySelector(expected)
    }

/**
 * Asserts that this storage NBT component has [expected] as its storage key.
 */
public infix fun StorageNBTComponent.shouldHaveStorageKey(expected: Key): StorageNBTComponent =
    apply {
        this should haveStorageKey(expected)
    }

/**
 * Returns this component's child at [index], or fails with a readable test error.
 */
public fun Component.childAt(index: Int): Component {
    val children = children()
    check(index in children.indices) {
        "Expected child at index <$index>, but component has <${children.size}> children."
    }
    return children[index]
}

private fun haveTextContent(expected: String): Matcher<Component> =
    Matcher { value ->
        val actual = value.textContent()
        MatcherResult(
            expected in actual,
            { "Expected component text to contain <$expected>, but was <$actual>." },
            { "Expected component text not to contain <$expected>." },
        )
    }

private fun Component.textContent(): String =
    buildString {
        appendText(this@textContent)
    }

private fun StringBuilder.appendText(component: Component) {
    if (component is TextComponent) {
        append(component.content())
    }
    component.children().forEach { child -> appendText(child) }
}

private fun haveColor(expected: TextColor): Matcher<Component> =
    Matcher { value ->
        val actual = value.color()
        MatcherResult(
            actual == expected,
            { "Expected component color <$expected>, but was <$actual>." },
            { "Expected component color not to be <$expected>." },
        )
    }

private fun haveNoColor(): Matcher<Component> =
    Matcher { value ->
        val actual = value.color()
        MatcherResult(
            actual == null,
            { "Expected component color to be absent, but was <$actual>." },
            { "Expected component color to be present." },
        )
    }

private fun haveStyle(expected: Style): Matcher<Component> =
    Matcher { value ->
        val actual = value.style()
        MatcherResult(
            actual == expected,
            { "Expected component style <$expected>, but was <$actual>." },
            { "Expected component style not to be <$expected>." },
        )
    }

private fun haveDecoration(expected: TextDecoration): Matcher<Component> = haveDecorationState(expected, State.TRUE)

private fun haveDecorationState(
    expected: TextDecoration,
    state: State,
): Matcher<Component> =
    Matcher { value ->
        val actual = value.style().decoration(expected)
        MatcherResult(
            actual == state,
            {
                "Expected component decoration <$expected> to be <${state.name}>, " +
                        "but was <${actual.name}>."
            },
            { "Expected component decoration <$expected> not to be <${state.name}>." },
        )
    }

private fun haveFont(expected: Key): Matcher<Component> =
    Matcher { value ->
        val actual = value.font()
        MatcherResult(
            actual == expected,
            { "Expected component font <$expected>, but was <${actual ?: "null"}>." },
            { "Expected component font not to be <$expected>." },
        )
    }

private fun haveNoFont(): Matcher<Component> =
    Matcher { value ->
        val actual = value.font()
        MatcherResult(
            actual == null,
            { "Expected component font to be absent, but was <$actual>." },
            { "Expected component font to be present." },
        )
    }

private fun haveInsertion(expected: String): Matcher<Component> =
    Matcher { value ->
        val actual = value.insertion()
        MatcherResult(
            actual == expected,
            { "Expected component insertion <$expected>, but was <${actual ?: "null"}>." },
            { "Expected component insertion not to be <$expected>." },
        )
    }

private fun haveNoInsertion(): Matcher<Component> =
    Matcher { value ->
        val actual = value.insertion()
        MatcherResult(
            actual == null,
            { "Expected component insertion to be absent, but was <$actual>." },
            { "Expected component insertion to be present." },
        )
    }

private fun haveClickEvent(expected: ClickEvent<*>): Matcher<Component> =
    Matcher { value ->
        val actual = value.clickEvent()
        MatcherResult(
            actual == expected,
            { "Expected click event <$expected>, but was <${actual ?: "null"}>." },
            { "Expected click event not to be <$expected>." },
        )
    }

private fun haveClickAction(expected: ClickEvent.Action<*>): Matcher<Component> =
    Matcher { value ->
        val actual = value.clickEvent()?.action()
        MatcherResult(
            actual == expected,
            { "Expected click action <$expected>, but was <${actual ?: "null"}>." },
            { "Expected click action not to be <$expected>." },
        )
    }

private fun haveClickTextPayload(expected: String): Matcher<Component> =
    Matcher { value ->
        val payload = value.clickEvent()?.payload()
        val actual = (payload as? ClickEvent.Payload.Text)?.value()
        MatcherResult(
            actual == expected,
            { "Expected click text payload <$expected>, but was <${actual ?: payloadDescription(payload)}>." },
            { "Expected click text payload not to be <$expected>." },
        )
    }

private fun haveClickIntPayload(expected: Int): Matcher<Component> =
    Matcher { value ->
        val payload = value.clickEvent()?.payload()
        val actual = (payload as? ClickEvent.Payload.Int)?.integer()
        MatcherResult(
            actual == expected,
            { "Expected click integer payload <$expected>, but was <${actual ?: payloadDescription(payload)}>." },
            { "Expected click integer payload not to be <$expected>." },
        )
    }

private fun haveNoClickEvent(): Matcher<Component> =
    Matcher { value ->
        val actual = value.clickEvent()
        MatcherResult(
            actual == null,
            { "Expected click event to be absent, but was <$actual>." },
            { "Expected click event to be present." },
        )
    }

private fun payloadDescription(payload: ClickEvent.Payload?): String =
    when (payload) {
        null -> "no click event"
        is ClickEvent.Payload.Text -> "text payload <${payload.value()}>"
        is ClickEvent.Payload.Int -> "integer payload <${payload.integer()}>"
        else -> payload.toString()
    }

private fun haveChildCount(expected: Int): Matcher<Component> =
    Matcher { value ->
        val actual = value.children().size
        MatcherResult(
            actual == expected,
            { "Expected <$expected> child components, but found <$actual>." },
            { "Expected child component count not to be <$expected>." },
        )
    }

private fun haveTranslationKey(expected: String): Matcher<Component> =
    Matcher { value ->
        val actual = value.translatableOrNull()?.key()
        MatcherResult(
            actual == expected,
            { "Expected translation key <$expected>, but was <${actual ?: "not translatable"}>." },
            { "Expected translation key not to be <$expected>." },
        )
    }

private fun haveFallback(expected: String): Matcher<Component> =
    Matcher { value ->
        val translatable = value.translatableOrNull()
        val actual = translatable?.fallback()
        val actualDescription = if (translatable == null) "not translatable" else actual ?: "null"
        MatcherResult(
            translatable != null && actual == expected,
            { "Expected translatable fallback <$expected>, but was <$actualDescription>." },
            { "Expected translatable fallback not to be <$expected>." },
        )
    }

private fun haveNoFallback(): Matcher<Component> =
    Matcher { value ->
        val translatable = value.translatableOrNull()
        val actual = translatable?.fallback()
        MatcherResult(
            translatable != null && actual == null,
            { "Expected translatable fallback to be absent, but was <${actual ?: "not translatable"}>." },
            { "Expected translatable fallback to be present." },
        )
    }

private fun haveArgumentCount(expected: Int): Matcher<Component> =
    Matcher { value ->
        val actual = value.translatableOrNull()?.arguments()?.size
        MatcherResult(
            actual == expected,
            { "Expected <$expected> translation arguments, but found <${actual ?: "not translatable"}>." },
            { "Expected translation argument count not to be <$expected>." },
        )
    }

private fun haveArguments(expected: List<TranslationArgument>): Matcher<Component> =
    Matcher { value ->
        val actual = value.translatableOrNull()?.arguments()
        MatcherResult(
            actual == expected,
            { "Expected translation arguments <$expected>, but found <${actual ?: "not translatable"}>." },
            { "Expected translation arguments not to be <$expected>." },
        )
    }

private inline fun <reified T : Component> Component.shouldBeComponentType(description: String): T =
    this as? T ?: throw AssertionError("Expected $description component, but was <${componentTypeName()}>.")

private fun Component.componentTypeName(): String = this::class.simpleName ?: this::class.qualifiedName ?: "unknown"

private fun haveKeybind(expected: String): Matcher<KeybindComponent> =
    Matcher { value ->
        val actual = value.keybind()
        MatcherResult(
            actual == expected,
            { "Expected keybind <$expected>, but was <$actual>." },
            { "Expected keybind not to be <$expected>." },
        )
    }

private fun haveScoreName(expected: String): Matcher<ScoreComponent> =
    Matcher { value ->
        val actual = value.name()
        MatcherResult(
            actual == expected,
            { "Expected score name <$expected>, but was <$actual>." },
            { "Expected score name not to be <$expected>." },
        )
    }

private fun haveScoreObjective(expected: String): Matcher<ScoreComponent> =
    Matcher { value ->
        val actual = value.objective()
        MatcherResult(
            actual == expected,
            { "Expected score objective <$expected>, but was <$actual>." },
            { "Expected score objective not to be <$expected>." },
        )
    }

private fun haveSelectorPattern(expected: String): Matcher<SelectorComponent> =
    Matcher { value ->
        val actual = value.pattern()
        MatcherResult(
            actual == expected,
            { "Expected selector pattern <$expected>, but was <$actual>." },
            { "Expected selector pattern not to be <$expected>." },
        )
    }

private fun haveSelectorSeparator(expected: Component): Matcher<SelectorComponent> =
    Matcher { value ->
        val actual = value.separator()
        MatcherResult(
            actual == expected,
            { "Expected selector separator <$expected>, but was <${actual ?: "null"}>." },
            { "Expected selector separator not to be <$expected>." },
        )
    }

private fun haveNoSelectorSeparator(): Matcher<SelectorComponent> =
    Matcher { value ->
        val actual = value.separator()
        MatcherResult(
            actual == null,
            { "Expected selector separator to be absent, but was <${actual ?: "null"}>." },
            { "Expected selector separator to be present." },
        )
    }

private fun haveObjectContents(expected: ObjectContents): Matcher<ObjectComponent> =
    Matcher { value ->
        val actual = value.contents()
        MatcherResult(
            actual == expected,
            { "Expected object contents <$expected>, but was <$actual>." },
            { "Expected object contents not to be <$expected>." },
        )
    }

private fun haveObjectFallback(expected: Component): Matcher<ObjectComponent> =
    Matcher { value ->
        val actual = value.fallback()
        MatcherResult(
            actual == expected,
            { "Expected object fallback <$expected>, but was <${actual ?: "null"}>." },
            { "Expected object fallback not to be <$expected>." },
        )
    }

private fun haveNoObjectFallback(): Matcher<ObjectComponent> =
    Matcher { value ->
        val actual = value.fallback()
        MatcherResult(
            actual == null,
            { "Expected object fallback to be absent, but was <${actual ?: "null"}>." },
            { "Expected object fallback to be present." },
        )
    }

private fun haveNbtPath(expected: String): Matcher<NBTComponent<*>> =
    Matcher { value ->
        val actual = value.nbtPath()
        MatcherResult(
            actual == expected,
            { "Expected NBT path <$expected>, but was <$actual>." },
            { "Expected NBT path not to be <$expected>." },
        )
    }

private fun haveInterpretState(expected: Boolean): Matcher<NBTComponent<*>> =
    Matcher { value ->
        val actual = value.interpret()
        MatcherResult(
            actual == expected,
            { "Expected NBT interpret to be <$expected>, but was <$actual>." },
            { "Expected NBT interpret not to be <$expected>." },
        )
    }

private fun haveNbtSeparator(expected: Component): Matcher<NBTComponent<*>> =
    Matcher { value ->
        val actual = value.separator()
        MatcherResult(
            actual == expected,
            { "Expected NBT separator <$expected>, but was <${actual ?: "null"}>." },
            { "Expected NBT separator not to be <$expected>." },
        )
    }

private fun haveNoNbtSeparator(): Matcher<NBTComponent<*>> =
    Matcher { value ->
        val actual = value.separator()
        MatcherResult(
            actual == null,
            { "Expected NBT separator to be absent, but was <${actual ?: "null"}>." },
            { "Expected NBT separator to be present." },
        )
    }

private fun haveBlockPos(expected: BlockNBTComponent.Pos): Matcher<BlockNBTComponent> =
    Matcher { value ->
        val actual = value.pos()
        MatcherResult(
            actual == expected,
            { "Expected block NBT position <${expected.asString()}>, but was <${actual.asString()}>." },
            { "Expected block NBT position not to be <${expected.asString()}>." },
        )
    }

private fun haveEntitySelector(expected: String): Matcher<EntityNBTComponent> =
    Matcher { value ->
        val actual = value.selector()
        MatcherResult(
            actual == expected,
            { "Expected entity NBT selector <$expected>, but was <$actual>." },
            { "Expected entity NBT selector not to be <$expected>." },
        )
    }

private fun haveStorageKey(expected: Key): Matcher<StorageNBTComponent> =
    Matcher { value ->
        val actual = value.storage()
        MatcherResult(
            actual == expected,
            { "Expected storage NBT key <$expected>, but was <$actual>." },
            { "Expected storage NBT key not to be <$expected>." },
        )
    }

private fun Component.translatableOrNull(): TranslatableComponent? = this as? TranslatableComponent
