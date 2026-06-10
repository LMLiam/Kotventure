package io.github.lmliam.kotventure.core.objectcomponent

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.`object`.ObjectContents
import net.kyori.adventure.text.`object`.SpriteObjectContents

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
