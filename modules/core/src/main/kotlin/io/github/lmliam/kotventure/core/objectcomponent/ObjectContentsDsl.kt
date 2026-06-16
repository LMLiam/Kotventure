package io.github.lmliam.kotventure.core.objectcomponent

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.`object`.ObjectContents
import net.kyori.adventure.text.`object`.PlayerHeadObjectContents
import net.kyori.adventure.text.`object`.SpriteObjectContents
import java.util.UUID

/**
 * Builds sprite object contents using Adventure's default sprite atlas.
 */
public fun sprite(sprite: Key): SpriteObjectContents = ObjectContents.sprite(sprite)

/**
 * Builds sprite object contents from [sprite] in [atlas].
 */
public fun sprite(
    atlas: Key,
    sprite: Key,
): SpriteObjectContents = ObjectContents.sprite(atlas, sprite)

/**
 * Builds player-head object contents for the player named [name], rendering the hat layer when [hat] is `true`.
 */
public fun head(
    name: String,
    hat: Boolean = PlayerHeadObjectContents.DEFAULT_HAT,
): PlayerHeadObjectContents =
    ObjectContents
    .playerHead()
    .name(name)
    .hat(hat)
    .build()

/**
 * Builds player-head object contents for the player with UUID [id], rendering the hat layer when [hat] is `true`.
 */
public fun head(
    id: UUID,
    hat: Boolean = PlayerHeadObjectContents.DEFAULT_HAT,
): PlayerHeadObjectContents =
    ObjectContents
    .playerHead()
    .id(id)
    .hat(hat)
    .build()

/**
 * Builds player-head object contents drawn from the skin [texture] key, rendering the hat layer when [hat] is `true`.
 */
public fun head(
    texture: Key,
    hat: Boolean = PlayerHeadObjectContents.DEFAULT_HAT,
): PlayerHeadObjectContents =
    ObjectContents
    .playerHead()
    .texture(texture)
    .hat(hat)
    .build()
