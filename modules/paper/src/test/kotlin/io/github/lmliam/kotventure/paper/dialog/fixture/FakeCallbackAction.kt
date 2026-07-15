package io.github.lmliam.kotventure.paper.dialog.fixture

import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.action.DialogActionCallback
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.api.BinaryTagHolder
import net.kyori.adventure.text.event.ClickCallback

/** Recording custom-click action capturing the registered [callback] and [options]. */
class FakeCallbackAction(
    val callback: DialogActionCallback,
    val options: ClickCallback.Options,
) : DialogAction.CustomClickAction {
    override fun id(): Key = error("not used")

    override fun additions(): BinaryTagHolder? = error("not used")
}
