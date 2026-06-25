package io.github.lmliam.kotventure.serializer

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.json.JSONOptions
import net.kyori.adventure.text.serializer.json.legacyimpl.NBTLegacyHoverEventSerializer

private val jsonSerializer: GsonComponentSerializer =
    GsonComponentSerializer
        .builder()
        .options(JSONOptions.compatibility())
        .legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.get())
        .build()

/**
 * Serializes this component to Adventure's JSON component format.
 */
public fun Component.toJson(): String = jsonSerializer.serialize(this)

/**
 * Deserializes this Adventure JSON component string to an Adventure component.
 */
public fun String.asJsonComponent(): Component = jsonSerializer.deserialize(this)
