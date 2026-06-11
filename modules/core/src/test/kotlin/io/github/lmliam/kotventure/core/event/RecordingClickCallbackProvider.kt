package io.github.lmliam.kotventure.core.event

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent

private const val RECORDED_CALLBACK_COMMAND = "__kotventure_callback__"
private const val UNSET_CALLBACK_COMMAND = "__kotventure_unset__"

internal class RecordingClickCallbackProvider : ClickCallback.Provider {
    override fun create(
        callback: ClickCallback<Audience>,
        options: ClickCallback.Options,
    ): ClickEvent<*> {
        lastCallback = callback
        lastOptions = options
        lastEvent = ClickEvent.suggestCommand(RECORDED_CALLBACK_COMMAND)
        return lastEvent
    }

    internal companion object {
        private val lastEventHolder: ThreadLocal<ClickEvent<*>> =
            ThreadLocal.withInitial { ClickEvent.suggestCommand(UNSET_CALLBACK_COMMAND) }
        private val lastCallbackHolder: ThreadLocal<ClickCallback<Audience>?> = ThreadLocal()
        private val lastOptionsHolder: ThreadLocal<ClickCallback.Options?> = ThreadLocal()

        internal var lastEvent: ClickEvent<*>
            get() = lastEventHolder.get()
            private set(value) {
                lastEventHolder.set(value)
            }
        internal var lastCallback: ClickCallback<Audience>?
            get() = lastCallbackHolder.get()
            private set(value) {
                lastCallbackHolder.set(value)
            }
        internal var lastOptions: ClickCallback.Options?
            get() = lastOptionsHolder.get()
            private set(value) {
                lastOptionsHolder.set(value)
            }

        internal fun reset() {
            lastEventHolder.set(ClickEvent.suggestCommand(UNSET_CALLBACK_COMMAND))
            lastCallbackHolder.remove()
            lastOptionsHolder.remove()
        }

        internal fun fire(audience: Audience) {
            checkNotNull(lastCallbackHolder.get()) { "Expected a recorded click callback." }.accept(audience)
        }
    }
}
