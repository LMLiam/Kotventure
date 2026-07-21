package io.github.lmliam.kotventure.coroutines.event

import io.github.lmliam.kotventure.core.audience.emptyAudience
import io.github.lmliam.kotventure.core.audience.message
import io.github.lmliam.kotventure.core.text.text
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import net.kyori.adventure.audience.Audience
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

private class Rewards {
    suspend fun claim(clicker: Audience) {
        delay(50.milliseconds)
        clicker.sendMessage(text("Reward claimed"))
    }
}

internal fun clickSample() {
    val audience = emptyAudience()
    val pluginScope = CoroutineScope(Dispatchers.Default)
    val rewards = Rewards()

    audience.message {
        text("Claim reward") {
            click(pluginScope) { clicker -> rewards.claim(clicker) }
        }
    }
}

internal fun clickOptionsSample() {
    val audience = emptyAudience()
    val pluginScope = CoroutineScope(Dispatchers.Default)
    val rewards = Rewards()

    audience.message {
        text("Claim reward") {
            click(
                pluginScope,
                options = {
                    uses(1)
                    lifetime(10.minutes)
                },
            ) { clicker -> rewards.claim(clicker) }
        }
    }
}

context(_: CoroutineScope)
private fun offerReward(
    audience: Audience,
    rewards: Rewards,
) {
    audience.message {
        text("Claim reward") {
            click { clicker -> rewards.claim(clicker) }
        }
    }
}

internal fun contextClickSample() {
    val audience = emptyAudience()
    val pluginScope = CoroutineScope(Dispatchers.Default)
    val rewards = Rewards()

    with(pluginScope) {
        offerReward(audience, rewards)
    }
}

context(_: CoroutineScope)
internal fun contextClickOptionsSample() {
    val audience = emptyAudience()
    val rewards = Rewards()

    audience.message {
        text("Claim reward") {
            click(options = { uses(1) }) { clicker ->
                rewards.claim(clicker)
            }
        }
    }
}

internal fun reusableClickSample() {
    val audience = emptyAudience()
    val broadcast = emptyAudience()
    val pluginScope = CoroutineScope(Dispatchers.Default)
    val rewards = Rewards()

    val claim = click(pluginScope) { clicker -> rewards.claim(clicker) }

    audience.message { text("[Claim]") { click(claim) } }
    broadcast.message { text("[Claim]") { click(claim) } }
}
