package io.github.lmliam.kotventure.paper.dialog

import io.github.lmliam.kotventure.core.audience.message
import io.github.lmliam.kotventure.core.text.text
import io.papermc.paper.dialog.Dialog
import net.kyori.adventure.audience.Audience

internal fun dialogSample(): Dialog =
    dialog(confirmation) {
        title { text("Daily reward") }
        externalTitle { text("Rewards") }
        closeOnEscape(false)
        afterAction(wait)
        message { text("Claim your daily reward?") }
        inputs {
            boolean("subscribe") {
                label { text("Subscribe to updates") }
                default()
            }
        }
        yes {
            label { text("Claim") }
            tooltip { text("Adds it to your inventory") }
            onClick { response, audience ->
                val subscribed = response.getBoolean("subscribe") == true
                audience.message { text("Claimed! Subscribed: $subscribed") }
            }
        }
        no { label { text("Later") } }
    }

internal fun showDialogSample(audience: Audience) {
    audience.dialog(notice) {
        title { text("Welcome") }
        message { text("Thanks for joining!") }
        button { label { text("Understood") } }
    }
}
