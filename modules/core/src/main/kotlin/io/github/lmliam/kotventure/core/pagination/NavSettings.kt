package io.github.lmliam.kotventure.core.pagination

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import kotlin.time.Duration

internal class NavSettings(
    val previous: Component,
    val next: Component,
    val indicator: (page: Int, pageCount: Int) -> ComponentLike?,
    val uses: Int,
    val lifetime: Duration,
)
