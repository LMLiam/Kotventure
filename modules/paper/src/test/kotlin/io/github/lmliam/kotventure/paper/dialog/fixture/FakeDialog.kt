package io.github.lmliam.kotventure.paper.dialog.fixture

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.type.DialogType
import org.bukkit.NamespacedKey

class FakeDialog(
    val base: DialogBase,
    val type: DialogType,
) : Dialog {
    override fun getKey(): NamespacedKey = error("not used")
}
