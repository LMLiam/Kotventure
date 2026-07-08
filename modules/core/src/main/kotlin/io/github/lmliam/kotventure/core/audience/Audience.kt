package io.github.lmliam.kotventure.core.audience

import net.kyori.adventure.audience.Audience

/**
 * Returns the [Audience] that silently ignores everything sent to it.
 *
 * @sample io.github.lmliam.kotventure.core.audience.emptyAudienceSample
 */
public fun emptyAudience(): Audience = Audience.empty()

/**
 * Creates a forwarding [Audience] that relays every operation to each of [members].
 *
 * @sample io.github.lmliam.kotventure.core.audience.audienceOfSample
 */
public fun audienceOf(vararg members: Audience): Audience = Audience.audience(*members)
