package io.github.lmliam.kotventure.paper.dialog.fixture

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry
import io.papermc.paper.registry.data.dialog.type.DialogType
import io.papermc.paper.registry.set.RegistryValueSetBuilder

/** Recording [DialogRegistryEntry.Builder] capturing the base and type set by the DSL. */
internal class FakeDialogRegistryEntryBuilder : DialogRegistryEntry.Builder {
    private var base: DialogBase? = null
    private var type: DialogType? = null

    override fun base(): DialogBase = checkNotNull(base) { "base not set" }

    override fun type(): DialogType = checkNotNull(type) { "type not set" }

    override fun base(dialogBase: DialogBase): DialogRegistryEntry.Builder = apply { base = dialogBase }

    override fun type(dialogType: DialogType): DialogRegistryEntry.Builder = apply { type = dialogType }

    override fun registryValueSet(): RegistryValueSetBuilder<Dialog, DialogRegistryEntry.Builder> = error("not used")
}
