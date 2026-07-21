package io.github.lmliam.kotventure.core.uuid

import java.util.UUID

/**
 * Parses [value] with [UUID.fromString].
 *
 * @throws IllegalArgumentException when [value] is not accepted by [UUID.fromString].
 */
public fun uuid(value: String): UUID = UUID.fromString(value)
