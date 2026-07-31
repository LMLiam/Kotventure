package io.github.lmliam.kotventure.minimessage.conversion

import io.github.lmliam.kotventure.core.selector.parseSelector
import net.kyori.adventure.text.BlockNBTComponent
import net.kyori.adventure.text.EntityNBTComponent
import net.kyori.adventure.text.NBTComponent
import net.kyori.adventure.text.ObjectComponent
import net.kyori.adventure.text.StorageNBTComponent

internal fun KotlinSourceBuilder.appendBlockNbt(component: BlockNBTComponent) =
    appendNbt(
        functionName = "blockNbt",
        sourceExpression = "blockPos(${quoted(component.pos().asString())})",
        component = component,
    )

internal fun KotlinSourceBuilder.appendEntityNbt(component: EntityNBTComponent) {
    val selector = parseSelector(component.selector())
    val interpret = component.interpret()
    val separator = component.separator()

    appendStructuredCall(
        function = "entityNbt",
        arguments =
            listOf(
                { appendEntitySelector(selector) },
                { line("nbtPath(${quoted(component.nbtPath())})") },
            ),
        component = component,
        hasAdditionalBody = interpret || separator != null,
    ) {
        if (interpret) line("interpret(true)")
        separator?.let { appendComponentArgument("separator", it) }
    }
}

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

    appendStructured(
        header = "$functionName($sourceExpression, nbtPath(${quoted(component.nbtPath())}))",
        component = component,
        hasAdditionalBody = interpret || separator != null,
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
        hasAdditionalBody = fallback != null,
    ) {
        fallback?.let { appendComponentArgument("fallback", it) }
    }
}
