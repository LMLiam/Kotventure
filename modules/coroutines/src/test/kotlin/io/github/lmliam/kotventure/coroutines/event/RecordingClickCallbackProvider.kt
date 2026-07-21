package io.github.lmliam.kotventure.coroutines.event

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent

private const val RECORDED_CALLBACK_COMMAND = "__kotventure_callback__"

internal class RecordingClickCallbackProvider : ClickCallback.Provider {
    override fun create(
        callback: ClickCallback<Audience>,
        options: ClickCallback.Options,
    ): ClickEvent<*> {
        val event = ClickEvent.suggestCommand(RECORDED_CALLBACK_COMMAND)

        lastCallbackHolder.set(callback)
        lastOptions = options
        lastEventHolder.set(event)

        return event
    }

    internal companion object {
        private val lastEventHolder: ThreadLocal<ClickEvent<*>?> = ThreadLocal()
        private val lastCallbackHolder: ThreadLocal<ClickCallback<Audience>?> = ThreadLocal()
        private val lastOptionsHolder: ThreadLocal<ClickCallback.Options?> = ThreadLocal()

        internal val lastEvent: ClickEvent<*>
            get() = checkNotNull(lastEventHolder.get()) { "Expected a recorded click event." }
        internal var lastOptions: ClickCallback.Options?
            get() = lastOptionsHolder.get()
            private set(value) {
                lastOptionsHolder.set(value)
            }

        internal fun reset() {
            lastEventHolder.remove()
            lastCallbackHolder.remove()
            lastOptionsHolder.remove()
        }

        internal fun fire(audience: Audience) {
            checkNotNull(lastCallbackHolder.get()) { "Expected a recorded click callback." }.accept(audience)
        }
    }
}
