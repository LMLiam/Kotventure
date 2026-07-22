package io.github.lmliam.kotventure.coroutines.prompt

import io.github.lmliam.kotventure.core.text.text

internal class UnlockedKitPrompt(
    unlocked: List<Kit>,
) : Prompt<Kit>({
    text("Choose a kit: ")
    unlocked.forEach { kit -> option(kit) { text(kit.label) } }
})
