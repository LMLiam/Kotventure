package io.github.lmliam.kotventure.coroutines.prompt

import io.github.lmliam.kotventure.core.text.text

internal object KitPrompt : Prompt<Kit>({
    text("Choose a kit: ")
    option(Kit.ARCHER) { text(Kit.ARCHER.label) }
    option(Kit.MAGE) { text(Kit.MAGE.label) }
})
