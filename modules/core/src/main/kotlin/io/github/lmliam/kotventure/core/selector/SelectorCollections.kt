package io.github.lmliam.kotventure.core.selector

import java.util.Collections

internal fun <T> Collection<T>.immutableSnapshot(): List<T> = Collections.unmodifiableList(ArrayList(this))
