package io.github.lmliam.kotventure.paper.dialog.fixture

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.RegistryBuilderFactory
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.data.InlinedRegistryBuilderProvider
import io.papermc.paper.registry.data.InstrumentRegistryEntry
import io.papermc.paper.registry.data.TrimMaterialRegistryEntry
import io.papermc.paper.registry.data.TrimPatternRegistryEntry
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry
import org.bukkit.MusicInstrument
import org.bukkit.inventory.meta.trim.TrimMaterial
import org.bukkit.inventory.meta.trim.TrimPattern
import java.util.function.Consumer

/**
 * Supplies an [InlinedRegistryBuilderProvider] to tests through `ServiceLoader`.
 *
 * The provider runs the registry consumer with a recording entry builder. It returns a [FakeDialog]
 * with the recorded base and type. Do not replace this class with a MockK proxy. [FakeRegistryAccess]
 * describes the related class-initialisation constraint.
 */
class FakeInlinedRegistryBuilderProvider : InlinedRegistryBuilderProvider {
    override fun createDialog(
        value: Consumer<RegistryBuilderFactory<Dialog, out DialogRegistryEntry.Builder>>,
    ): Dialog {
        val entry = FakeDialogRegistryEntryBuilder()
        value.accept(
            object : RegistryBuilderFactory<Dialog, DialogRegistryEntry.Builder> {
                override fun empty(): DialogRegistryEntry.Builder = entry

                override fun copyFrom(key: TypedKey<Dialog>): DialogRegistryEntry.Builder = error("not used")
            },
        )
        return FakeDialog(entry.base(), entry.type())
    }

    override fun createInstrument(
        value: Consumer<RegistryBuilderFactory<MusicInstrument, out InstrumentRegistryEntry.Builder>>,
    ): MusicInstrument = error("not used")

    override fun createTrimMaterial(
        value: Consumer<RegistryBuilderFactory<TrimMaterial, out TrimMaterialRegistryEntry.Builder>>,
    ): TrimMaterial = error("not used")

    override fun createTrimPattern(
        value: Consumer<RegistryBuilderFactory<TrimPattern, out TrimPatternRegistryEntry.Builder>>,
    ): TrimPattern = error("not used")
}
