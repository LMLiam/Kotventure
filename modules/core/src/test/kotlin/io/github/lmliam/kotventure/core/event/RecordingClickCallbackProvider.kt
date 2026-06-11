package io.github.lmliam.kotventure.core.event

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent

internal class RecordingClickCallbackProvider : ClickCallback.Provider {
    override fun create(
        callback: ClickCallback<Audience>,
        options: ClickCallback.Options,
    ): ClickEvent<*> {
        lastCallback = callback
        lastOptions = options
        lastEvent = ClickEvent.suggestCommand("__kotventure_callback__")
        return lastEvent
    }

    internal companion object {
        internal lateinit var lastEvent: ClickEvent<*>
            private set
        internal var lastCallback: ClickCallback<Audience>? = null
            private set
        internal var lastOptions: ClickCallback.Options? = null
            private set

        internal fun reset() {
            lastEvent = ClickEvent.suggestCommand("__kotventure_unset__")
            lastCallback = null
            lastOptions = null
        }

        internal fun fire(audience: Audience) {
            checkNotNull(lastCallback) { "Expected a recorded click callback." }.accept(audience)
        }
    }
}
