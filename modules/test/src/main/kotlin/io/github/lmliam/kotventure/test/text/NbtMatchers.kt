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
 * Returns a matcher that compares the NBT path with [expected].
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
 * Returns a matcher that compares the NBT path with [expected].
 */
public fun haveNbtPath(expected: NbtPath): Matcher<NBTComponent<*>> = haveNbtPath(expected.asString())

/**
 * Returns a matcher that compares the NBT interpret state with [expected].
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
 * Returns a matcher that compares the NBT separator with [expected].
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
 * Returns a matcher that accepts an NBT component without a separator.
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
 * Returns a matcher that compares the block position with [expected].
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
 * Returns a matcher that compares the entity selector with [expected].
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
 * Returns a matcher that compares the storage key with [expected].
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
 * Verifies that this component is a [BlockNBTComponent].
 *
 * @return this component as a [BlockNBTComponent].
 * @throws AssertionError when this component has a different type.
 */
public fun Component.shouldBeBlockNbtComponent(): BlockNBTComponent = asComponentType("block NBT")

/**
 * Verifies that this component is an [EntityNBTComponent].
 *
 * @return this component as an [EntityNBTComponent].
 * @throws AssertionError when this component has a different type.
 */
public fun Component.shouldBeEntityNbtComponent(): EntityNBTComponent = asComponentType("entity NBT")

/**
 * Verifies that this component is a [StorageNBTComponent].
 *
 * @return this component as a [StorageNBTComponent].
 * @throws AssertionError when this component has a different type.
 */
public fun Component.shouldBeStorageNbtComponent(): StorageNBTComponent = asComponentType("storage NBT")

/**
 * Verifies that this NBT component has the path [expected].
 */
public infix fun NBTComponent<*>.shouldHaveNbtPath(expected: String): NBTComponent<*> =
    apply {
        this should haveNbtPath(expected)
    }

/**
 * Verifies that this NBT component has the path [expected].
 */
public infix fun NBTComponent<*>.shouldHaveNbtPath(expected: NbtPath): NBTComponent<*> =
    apply {
        this should haveNbtPath(expected)
    }

/**
 * Verifies that this NBT component interprets fetched NBT as component JSON.
 */
public fun NBTComponent<*>.shouldInterpret(): NBTComponent<*> =
    apply {
        this should haveInterpretState(true)
    }

/**
 * Verifies that this NBT component does not interpret fetched NBT as component JSON.
 */
public fun NBTComponent<*>.shouldNotInterpret(): NBTComponent<*> =
    apply {
        this should haveInterpretState(false)
    }

/**
 * Verifies that this NBT component has the separator [expected].
 */
public infix fun <T : ComponentLike> NBTComponent<*>.shouldHaveNbtSeparator(expected: T): NBTComponent<*> =
    apply {
        this should haveNbtSeparator(expected)
    }

/**
 * Verifies that this NBT component has no separator.
 */
public fun NBTComponent<*>.shouldNotHaveNbtSeparator(): NBTComponent<*> =
    apply {
        this should haveNoNbtSeparator()
    }

/**
 * Verifies that this block NBT component has the position [expected].
 */
public infix fun BlockNBTComponent.shouldHaveBlockPos(expected: BlockNBTComponent.Pos): BlockNBTComponent =
    apply {
        this should haveBlockPos(expected)
    }

/**
 * Verifies that this entity NBT component has the selector [expected].
 */
public infix fun EntityNBTComponent.shouldHaveEntitySelector(expected: String): EntityNBTComponent =
    apply {
        this should haveEntitySelector(expected)
    }

/**
 * Verifies that this entity NBT component has the selector [expected].
 */
public infix fun EntityNBTComponent.shouldHaveEntitySelector(expected: EntitySelector): EntityNBTComponent =
    apply {
        this should haveEntitySelector(expected.asString())
    }

/**
 * Verifies that this storage NBT component has the storage key [expected].
 */
public infix fun StorageNBTComponent.shouldHaveStorageKey(expected: Key): StorageNBTComponent =
    apply {
        this should haveStorageKey(expected)
    }
