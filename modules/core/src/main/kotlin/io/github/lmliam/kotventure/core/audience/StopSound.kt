package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.sound.SoundStopBuilder
import io.github.lmliam.kotventure.core.sound.SoundStopScope
import net.kyori.adventure.audience.Audience

/**
 * Builds a [net.kyori.adventure.sound.SoundStop] from [init] and stops matching sounds on this
 * [Audience], forwarding to Adventure's native [Audience.stopSound].
 *
 * Supported shapes inside [init]:
 * - `named(key)` → stop by sound name
 * - `named(key); source(music)` → stop that name on that source
 * - `source(music)` → stop every sound on that source
 * - `all()` → stop everything
 *
 * An empty block is rejected so stopping everything is never a silent default.
 *
 * Works for any audience — a player, the console, or a forwarding audience over many members;
 * audiences without a sound surface ignore it.
 *
 * @throws IllegalStateException when the block is empty, sets any slot twice, or combines `all`
 *   with `named`/`source`.
 * @sample io.github.lmliam.kotventure.core.audience.audienceStopSoundSample
 */
public fun Audience.stopSound(init: SoundStopScope.() -> Unit): Unit = stopSound(SoundStopBuilder().apply(init).build())
