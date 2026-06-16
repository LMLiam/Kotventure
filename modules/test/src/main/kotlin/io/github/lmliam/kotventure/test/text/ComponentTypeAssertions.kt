package io.github.lmliam.kotventure.test.text

import net.kyori.adventure.text.Component

/**
 * Casts this component to [T], or throws an [AssertionError] describing the actual component type.
 *
 * Shared by the type-narrowing matchers (`shouldBeKeybindComponent`, `shouldBeScoreComponent`, …) so they report a
 * consistent failure message when the component is of an unexpected type.
 */
internal inline fun <reified T : Component> Component.asComponentType(description: String): T =
    this as? T ?: throw AssertionError("Expected $description component, but was <${componentTypeName()}>.")

/** Returns a human-readable name for this component's concrete type. */
internal fun Component.componentTypeName(): String = this::class.simpleName ?: this::class.qualifiedName ?: "unknown"
