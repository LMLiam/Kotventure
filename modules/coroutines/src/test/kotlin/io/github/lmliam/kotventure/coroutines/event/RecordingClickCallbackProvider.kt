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
        val recorded = recordings
        val event = ClickEvent.suggestCommand("$RECORDED_CALLBACK_COMMAND${recorded.size}")

        recorded += RecordedClickCallback(event, callback, options)

        return event
    }

    internal companion object {
        private val recordingsHolder: ThreadLocal<MutableList<RecordedClickCallback>> =
            ThreadLocal.withInitial { mutableListOf() }

        private val recordings: MutableList<RecordedClickCallback>
            get() = recordingsHolder.get()

        internal val lastEvent: ClickEvent<*>
            get() = last().event

        internal val lastOptions: ClickCallback.Options?
            get() = recordings.lastOrNull()?.options

        internal fun eventAt(index: Int): ClickEvent<*> = recordings[index].event

        internal fun optionsAt(index: Int): ClickCallback.Options = recordings[index].options

        internal fun recordedCount(): Int = recordings.size

        internal fun reset() {
            recordingsHolder.remove()
        }

        internal fun fire(audience: Audience) {
            last().callback.accept(audience)
        }

        internal fun fire(
            index: Int,
            audience: Audience,
        ) {
            recordings[index].callback.accept(audience)
        }

        private fun last(): RecordedClickCallback =
            checkNotNull(recordings.lastOrNull()) { "Expected a recorded click callback." }
    }
}
