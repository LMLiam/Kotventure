package io.github.lmliam.kotventure.core.objectcomponent

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.`object`.ObjectContents
import net.kyori.adventure.text.`object`.PlayerHeadObjectContents
import net.kyori.adventure.text.`object`.SpriteObjectContents
import java.util.UUID

/**
 * Creates sprite contents for [sprite] in the default Adventure sprite atlas.
 */
public fun sprite(sprite: Key): SpriteObjectContents = ObjectContents.sprite(sprite)

/**
 * Creates sprite contents for [sprite] in [atlas].
 */
public fun sprite(
    atlas: Key,
    sprite: Key,
): SpriteObjectContents = ObjectContents.sprite(atlas, sprite)

/**
 * Creates player-head contents for the player named [name].
 *
 * The client renders the hat layer when [hat] is `true`.
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
 * Creates player-head contents for the player with UUID [id].
 *
 * The client renders the hat layer when [hat] is `true`.
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
 * Creates player-head contents from the skin [texture].
 *
 * The client renders the hat layer when [hat] is `true`.
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
