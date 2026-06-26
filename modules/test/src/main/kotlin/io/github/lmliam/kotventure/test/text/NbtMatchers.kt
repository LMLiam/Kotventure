package io.github.lmliam.kotventure.test.text

import io.github.lmliam.kotventure.core.nbt.NbtPath
import io.github.lmliam.kotventure.core.selector.EntitySelector
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.BlockNBTComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.EntityNBTComponent
import net.kyori.adventure.text.NBTComponent
import net.kyori.adventure.text.StorageNBTComponent

/**
 * Matches an NBT component whose path is [expected]. Combine with `and`/`or` or negate with `shouldNot`.
 */
public fun haveNbtPath(expected: String): Matcher<NBTComponent<*>> =
    Matcher { value ->
        val actual = value.nbtPath()
        MatcherResult(
            actual == expected,
            { "Expected NBT path <$expected>, but was <$actual>." },
            { "Expected NBT path not to be <$expected>." },
        )
    }

/**
 * Matches an NBT component whose path is [expected]. Combine with `and`/`or` or negate with `shouldNot`.
 */
public fun haveNbtPath(expected: NbtPath): Matcher<NBTComponent<*>> = haveNbtPath(expected.asString())

/**
 * Matches an NBT component whose interpret flag is [expected].
 */
public fun haveInterpretState(expected: Boolean): Matcher<NBTComponent<*>> =
    Matcher { value ->
        val actual = value.interpret()
        MatcherResult(
            actual == expected,
            { "Expected NBT interpret to be <$expected>, but was <$actual>." },
            { "Expected NBT interpret not to be <$expected>." },
        )
    }

/**
 * Matches an NBT component whose separator is [expected].
 */
public fun <T : ComponentLike> haveNbtSeparator(expected: T): Matcher<NBTComponent<*>> =
    Matcher { value ->
        val actual = value.separator()
        val expectedComponent = expected.asComponent()
        MatcherResult(
            actual == expectedComponent,
            { "Expected NBT separator <$expectedComponent>, but was <${actual ?: "null"}>." },
            { "Expected NBT separator not to be <$expectedComponent>." },
        )
    }

/**
 * Matches an NBT component that has no separator.
 */
public fun haveNoNbtSeparator(): Matcher<NBTComponent<*>> =
    Matcher { value ->
        val actual = value.separator()
        MatcherResult(
            actual == null,
            { "Expected NBT separator to be absent, but was <${actual ?: "null"}>." },
            { "Expected NBT separator to be present." },
        )
    }

/**
 * Matches a block NBT component whose block position is [expected].
 */
public fun haveBlockPos(expected: BlockNBTComponent.Pos): Matcher<BlockNBTComponent> =
    Matcher { value ->
        val actual = value.pos()
        MatcherResult(
            actual == expected,
            { "Expected block NBT position <${expected.asString()}>, but was <${actual.asString()}>." },
            { "Expected block NBT position not to be <${expected.asString()}>." },
        )
    }

/**
 * Matches an entity NBT component whose selector is [expected].
 */
public fun haveEntitySelector(expected: String): Matcher<EntityNBTComponent> =
    Matcher { value ->
        val actual = value.selector()
        MatcherResult(
            actual == expected,
            { "Expected entity NBT selector <$expected>, but was <$actual>." },
            { "Expected entity NBT selector not to be <$expected>." },
        )
    }

/**
 * Matches a storage NBT component whose storage key is [expected].
 */
public fun haveStorageKey(expected: Key): Matcher<StorageNBTComponent> =
    Matcher { value ->
        val actual = value.storage()
        MatcherResult(
            actual == expected,
            { "Expected storage NBT key <$expected>, but was <$actual>." },
            { "Expected storage NBT key not to be <$expected>." },
        )
    }

/**
 * Asserts that this component is a [BlockNBTComponent] and returns it typed.
 */
public fun Component.shouldBeBlockNbtComponent(): BlockNBTComponent = asComponentType("block NBT")

/**
 * Asserts that this component is an [EntityNBTComponent] and returns it typed.
 */
public fun Component.shouldBeEntityNbtComponent(): EntityNBTComponent = asComponentType("entity NBT")

/**
 * Asserts that this component is a [StorageNBTComponent] and returns it typed.
 */
public fun Component.shouldBeStorageNbtComponent(): StorageNBTComponent = asComponentType("storage NBT")

/**
 * Asserts that this NBT component has [expected] as its NBT path.
 */
public infix fun NBTComponent<*>.shouldHaveNbtPath(expected: String): NBTComponent<*> =
    apply {
        this should haveNbtPath(expected)
    }

/**
 * Asserts that this NBT component has [expected] as its NBT path.
 */
public infix fun NBTComponent<*>.shouldHaveNbtPath(expected: NbtPath): NBTComponent<*> =
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
public infix fun <T : ComponentLike> NBTComponent<*>.shouldHaveNbtSeparator(expected: T): NBTComponent<*> =
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
 * Asserts that this entity NBT component has [expected] as its selector.
 */
public infix fun EntityNBTComponent.shouldHaveEntitySelector(expected: EntitySelector): EntityNBTComponent =
    apply {
        this should haveEntitySelector(expected.asString())
    }

/**
 * Asserts that this storage NBT component has [expected] as its storage key.
 */
public infix fun StorageNBTComponent.shouldHaveStorageKey(expected: Key): StorageNBTComponent =
    apply {
        this should haveStorageKey(expected)
    }
