package io.github.lmliam.kotventure.core.selector

import java.util.Collections

/**
 * Copies this collection and prevents mutation even after an unsafe downcast to [MutableList].
 */
internal fun <T> Collection<T>.immutableSnapshot(): List<T> = Collections.unmodifiableList(ArrayList(this))
