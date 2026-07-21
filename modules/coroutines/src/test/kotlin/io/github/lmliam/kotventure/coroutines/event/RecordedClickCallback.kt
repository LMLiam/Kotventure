package io.github.lmliam.kotventure.coroutines.event

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent

internal class RecordedClickCallback(
    val event: ClickEvent<*>,
    val callback: ClickCallback<Audience>,
    val options: ClickCallback.Options,
)
