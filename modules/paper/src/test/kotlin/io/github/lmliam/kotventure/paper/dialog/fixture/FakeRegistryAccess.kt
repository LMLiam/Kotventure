@file:Suppress("UnstableApiUsage")

package io.github.lmliam.kotventure.paper.dialog.fixture

import io.github.lmliam.kotventure.paper.item.fixture.FakeDataComponentRegistry
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.NoticeType
import net.kyori.adventure.text.Component
import org.bukkit.Keyed
import org.bukkit.Registry

/**
 * ServiceLoader-registered [RegistryAccess] so that initializing registry-backed Paper constants
 * succeeds in a plain unit test without a running server. Dialog lookups return a placeholder
 * [FakeDialog], while the data-component registry returns keyed placeholder component types.
 *
 * This runs from inside [org.bukkit.Registry]'s own class initialization (its `<clinit>` eagerly
 * builds legacy registries for every registry type, [Dialog] included), so every placeholder here
 * is constructed fresh per call rather than read from a Kotlin `object` singleton or built as a
 * mockk stub. Both alternatives read a static field during their own initialization: a
 * singleton's backing field is only assigned once its class finishes initializing, and a mockk
 * proxy forces its target interface to finish initializing before objenesis can instantiate it.
 * [Registry] itself is the class already mid-initialization on this thread, so either alternative
 * re-enters that same not-yet-finished `<clinit>`, reads the not-yet-assigned field, and throws
 * (`NullPointerException` for the singleton, `ExceptionInInitializerError` for mockk). A plain
 * constructor call has no such field to race.
 */
class FakeRegistryAccess : RegistryAccess {
    @Suppress("OVERRIDE_DEPRECATION")
    override fun <T : Keyed> getRegistry(type: Class<T>): Registry<T> = registry()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Keyed> getRegistry(key: RegistryKey<T>): Registry<T> =
        if (key.key().value() == "data_component_type") {
            FakeDataComponentRegistry() as Registry<T>
        } else {
            registry()
        }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Keyed> registry(): Registry<T> =
        FakeDialogRegistry(FakeDialog(placeholderBase(), placeholderType())) as Registry<T>

    private fun placeholderBase(): DialogBase =
        object : DialogBase {
            override fun title(): Component = Component.text("")

            override fun externalTitle(): Component? = null

            override fun canCloseWithEscape(): Boolean = true

            override fun pause(): Boolean = true

            override fun afterAction(): DialogBase.DialogAfterAction = DialogBase.DialogAfterAction.CLOSE

            override fun body(): List<DialogBody> = emptyList()

            override fun inputs(): List<DialogInput> = emptyList()
        }

    private fun placeholderType(): NoticeType = NoticeType { error("not used") }
}
