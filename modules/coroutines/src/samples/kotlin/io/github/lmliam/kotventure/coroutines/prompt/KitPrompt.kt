package io.github.lmliam.kotventure.coroutines.prompt

import io.github.lmliam.kotventure.core.color.aqua
import io.github.lmliam.kotventure.core.color.green
import io.github.lmliam.kotventure.core.text.text

internal object KitPrompt : Prompt<Kit>({
    text("Choose a kit: ")
    option(Kit.ARCHER) { text("[Archer]") { color(green) } }
    option(Kit.MAGE) { text("[Mage]") { color(aqua) } }
})
