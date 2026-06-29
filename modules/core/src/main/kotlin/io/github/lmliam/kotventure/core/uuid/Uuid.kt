package io.github.lmliam.kotventure.core.uuid

import java.util.UUID

/**
 * Parses a [UUID] from its standard string [value].
 *
 * @throws IllegalArgumentException if [value] is not a valid UUID string.
 */
public fun uuid(value: String): UUID = UUID.fromString(value)
