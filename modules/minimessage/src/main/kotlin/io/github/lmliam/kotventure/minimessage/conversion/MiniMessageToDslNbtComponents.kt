package io.github.lmliam.kotventure.minimessage.conversion

import net.kyori.adventure.text.BlockNBTComponent
import net.kyori.adventure.text.EntityNBTComponent
import net.kyori.adventure.text.NBTComponent
import net.kyori.adventure.text.ObjectComponent
import net.kyori.adventure.text.StorageNBTComponent

internal fun KotlinSourceBuilder.appendBlockNbt(component: BlockNBTComponent) {
    appendNbt("blockNbt", "blockPos(\"${escapeKotlinString(component.pos().asString())}\")", component)
}

internal fun KotlinSourceBuilder.appendEntityNbt(component: EntityNBTComponent) {
    appendNbt("entityNbt", "\"${escapeKotlinString(component.selector())}\"", component)
}

internal fun KotlinSourceBuilder.appendStorageNbt(component: StorageNBTComponent) {
    appendNbt("storageNbt", keyLiteral(component.storage()), component)
}

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
