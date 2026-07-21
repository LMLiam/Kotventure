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
 * Serialises this component to Adventure JSON.
 *
 * The shared serialiser uses Adventure compatibility options and supports legacy hover-event data.
 *
 * @return the JSON representation of this component.
 */
public fun Component.toJson(): String = jsonSerializer.serialize(this)

/**
 * Deserialises the receiver Adventure JSON to a component.
 *
 * The shared serialiser accepts legacy hover-event data.
 *
 * @return the component represented by the receiver JSON.
 * @throws com.google.gson.JsonParseException when Adventure cannot parse the JSON or component data.
 */
public fun String.asJsonComponent(): Component = jsonSerializer.deserialize(this)
