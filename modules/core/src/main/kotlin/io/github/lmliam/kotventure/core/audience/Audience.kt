package io.github.lmliam.kotventure.core.audience

import net.kyori.adventure.audience.Audience

/**
 * Returns the [Audience] that silently ignores everything sent to it.
 *
 * @sample io.github.lmliam.kotventure.core.audience.emptyAudienceSample
 */
public fun emptyAudience(): Audience = Audience.empty()

/**
 * Creates an [Audience] that forwards each operation to every entry in [members].
 *
 * An empty argument list returns an empty audience. The function does not send an operation when it creates the
 * forwarding audience.
 *
 * @sample io.github.lmliam.kotventure.core.audience.audienceOfSample
 */
public fun audienceOf(vararg members: Audience): Audience = Audience.audience(*members)
