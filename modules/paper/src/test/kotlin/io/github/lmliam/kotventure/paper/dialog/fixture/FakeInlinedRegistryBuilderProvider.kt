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
 * A ServiceLoader-registered [InlinedRegistryBuilderProvider] for the dialog tests.
 *
 * It runs the DSL registry consumer against a recording entry builder. It then returns a [FakeDialog] with the captured
 * base and type. The registry class has the initialisation constraint that [FakeRegistryAccess] describes. Therefore,
 * do not use MockK on this path.
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
