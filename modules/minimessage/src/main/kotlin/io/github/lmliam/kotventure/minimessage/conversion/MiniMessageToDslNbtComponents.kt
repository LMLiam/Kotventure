package io.github.lmliam.kotventure.minimessage.conversion

import net.kyori.adventure.text.BlockNBTComponent
import net.kyori.adventure.text.EntityNBTComponent
import net.kyori.adventure.text.NBTComponent
import net.kyori.adventure.text.ObjectComponent
import net.kyori.adventure.text.StorageNBTComponent

private fun quoted(value: String): String = "\"${escapeKotlinString(value)}\""

internal fun KotlinSourceBuilder.appendBlockNbt(component: BlockNBTComponent) =
    appendNbt(
        functionName = "blockNbt",
        sourceExpression = "blockPos(${quoted(component.pos().asString())})",
        component = component,
    )

internal fun KotlinSourceBuilder.appendEntityNbt(component: EntityNBTComponent) =
    appendNbt(
        functionName = "entityNbt",
        sourceExpression = entitySelectorLiteral(component.selector()),
        component = component,
    )

internal fun KotlinSourceBuilder.appendStorageNbt(component: StorageNBTComponent) =
    appendNbt(
        functionName = "storageNbt",
        sourceExpression = keyLiteral(component.storage()),
        component = component,
    )

private fun KotlinSourceBuilder.appendNbt(
    functionName: String,
    sourceExpression: String,
    component: NBTComponent<*>,
) {
    val interpret = component.interpret()
    val separator = component.separator()
    val hasExtraBody = interpret || separator != null
    val nbtPath = quoted(component.nbtPath())

    appendStructured(
        header = "$functionName($sourceExpression, nbtPath($nbtPath))",
        component = component,
        hasExtraBody = hasExtraBody,
    ) {
        if (interpret) line("interpret(true)")
        separator?.let { appendComponentArgument("separator", it) }
    }
}

internal fun KotlinSourceBuilder.appendObject(component: ObjectComponent) {
    val fallback = component.fallback()

    appendStructured(
        header = "display(${objectContentsLiteral(component.contents())})",
        component = component,
        hasExtraBody = fallback != null,
    ) {
        fallback?.let { appendComponentArgument("fallback", it) }
    }
}
