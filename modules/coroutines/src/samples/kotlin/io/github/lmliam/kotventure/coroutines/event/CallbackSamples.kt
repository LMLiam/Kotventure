package io.github.lmliam.kotventure.coroutines.event

import io.github.lmliam.kotventure.core.audience.emptyAudience
import io.github.lmliam.kotventure.core.audience.message
import io.github.lmliam.kotventure.core.text.text
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.event.ClickCallback
import kotlin.time.Duration.Companion.minutes

private class Rewards {
    suspend fun claim(clicker: Audience) {
        clicker.sendMessage(text("Reward claimed"))
    }
}

internal fun callbackSample() {
    val audience = emptyAudience()
    val pluginScope = CoroutineScope(Dispatchers.Default)
    val rewards = Rewards()

    audience.message {
        text("Claim reward") {
            click { callback(pluginScope) { clicker -> rewards.claim(clicker) } }
        }
    }
}

internal fun callbackUsesLifetimeSample() {
    val audience = emptyAudience()
    val pluginScope = CoroutineScope(Dispatchers.Default)
    val rewards = Rewards()

    audience.message {
        text("Claim reward") {
            click {
                callback(pluginScope, uses = 1, lifetime = 10.minutes) { clicker ->
                    rewards.claim(clicker)
                }
            }
        }
    }
}

internal fun callbackOptionsSample() {
    val audience = emptyAudience()
    val pluginScope = CoroutineScope(Dispatchers.Default)
    val rewards = Rewards()
    val options =
        ClickCallback.Options
                .builder()
                .uses(1)
                .build()

    audience.message {
        text("Claim reward") {
            click { callback(pluginScope, options) { clicker -> rewards.claim(clicker) } }
        }
    }
}
