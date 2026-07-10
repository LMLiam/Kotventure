package io.github.lmliam.kotventure.core.sound

import io.github.lmliam.kotventure.core.dsl.once
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.SoundStop

internal class SoundStopBuilder : SoundStopScope {
    private var all: Boolean? by once()
    private var named: Key? by once()
    private var source: Sound.Source? by once()

    override fun all() {
        check(named == null && source == null) {
            "'all' cannot be set when 'named' or 'source' is already set."
        }
        all = true
    }

    override fun named(name: Key) {
        check(all == null) { "'named' cannot be set when 'all' is already set." }
        named = name
    }

    override fun source(source: Sound.Source) {
        check(all == null) { "'source' cannot be set when 'all' is already set." }
        this.source = source
    }

    internal fun build(): SoundStop {
        // once()-delegated slots never smart-cast; locals do.
        val named = named
        val source = source
        return when {
            all != null -> SoundStop.all()
            named != null && source != null -> SoundStop.namedOnSource(named, source)
            named != null -> SoundStop.named(named)
            source != null -> SoundStop.source(source)
            else -> throw IllegalStateException(
                "At least one of 'all', 'named', or 'source' must be set.",
            )
        }
    }
}
