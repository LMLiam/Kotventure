package io.github.lmliam.kotventure.minimessage.readme

import io.github.lmliam.kotventure.core.audience.bossBar
import io.github.lmliam.kotventure.core.audience.message
import io.github.lmliam.kotventure.core.audience.sound
import io.github.lmliam.kotventure.core.audience.title
import io.github.lmliam.kotventure.core.color.aqua
import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.color.gray
import io.github.lmliam.kotventure.core.color.green
import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.core.time.Ticker
import io.github.lmliam.kotventure.core.time.ticks
import io.github.lmliam.kotventure.minimessage.template.bind
import io.github.lmliam.kotventure.minimessage.template.invoke
import net.kyori.adventure.audience.Audience
import kotlin.time.Duration.Companion.seconds

internal fun readmeScenarioSample(
    joiner: Audience,
    everyone: Audience,
    name: String,
    onlineCount: Int,
    ticker: Ticker,
) {
    val nameplate =
        text(name) {
            style(EventTheme.header)
            hover { text("Add $name as a friend") }
            click { suggest("/friend add $name") }
        }

    everyone.message {
        append(
            JoinBroadcast {
                player bind nameplate
                online bind onlineCount
            },
        )
    }

    joiner.title {
        title { text("Sky Games") { gradient(aqua, green, gold) } }
        subtitle { text("Season 5 — Capture the Core") { color(gray) } }
        times {
            fadeIn(10.ticks)
            stay(3.seconds)
            fadeOut(10.ticks)
        }
    }

    joiner.sound(key("minecraft:ui.toast.challenge_complete")) {
        volume(0.8f)
        pitch(1.2f)
    }

    context(ticker) {
        joiner.bossBar(over = 60.seconds) {
            name { remaining -> text("Round starts in ${remaining.inWholeSeconds}s") }
            color(green)
            overlay(notched10)
            progress(from = 0f, to = 1f)
            every(1.ticks)
        }
    }
}
