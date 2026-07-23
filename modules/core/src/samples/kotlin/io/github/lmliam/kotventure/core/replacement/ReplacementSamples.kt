package io.github.lmliam.kotventure.core.replacement

import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.color.red
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.core.translatable.translatable

internal fun replaceLiteralSample() {
    val message = text("Welcome, %player%!")
    val greeting = message.replace("%player%") { replacement("Alex") { color(gold) } }
}

internal fun replaceModifySample() {
    val line = text("user: alex#42")
    val tagged =
        line.replace(Regex("""(?<user>\w+)#(\d+)""")) {
            modify {
                content(match["user"].orEmpty())
                insertion(match[2].orEmpty())
                color(gold)
            }
        }
}

internal fun replaceComponentSample() {
    val badge = text("VIP") { color(gold) }
    val message = text("Welcome, %badge%!")
    val decorated = message.replace("%badge%") { replacement(badge) }
}

internal fun replaceLimitSample() {
    val message = text("%tip% %tip% %tip%")
    val firstOnly =
        message.replace("%tip%") {
            once()
            replacement("tip")
        }
    val threeOfThem =
        message.replace("%tip%") {
            times(3)
            replacement("tip")
        }
}

internal fun replaceConditionSample() {
    val chat = text("visit https://example.com or example.net")
    val linked =
        chat.replace(Regex("""\S+\.\S+""")) {
            condition { if (match.value.startsWith("https://")) replace else skip }
            modify {
                val url = match.value
                color(red)
                click { openUrl(url) }
            }
        }
}

internal fun replaceRemoveSample() {
    val line = text("role: %role%")
    val roles =
        line.replace(Regex("""role: (\w+)""")) {
            replacement {
                val role = match[1].orEmpty()
                if (role == "hidden") remove else translatable("role.$role")
            }
        }
}
