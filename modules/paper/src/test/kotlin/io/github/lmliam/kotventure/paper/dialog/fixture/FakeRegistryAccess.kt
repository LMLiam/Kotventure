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
 * Supplies [RegistryAccess] to tests through `ServiceLoader` when no server is running.
 *
 * Dialog lookups return a placeholder [FakeDialog]. Data-component lookups return keyed placeholder
 * component types. Paper calls this provider while it initialises [org.bukkit.Registry].
 *
 * Construct a new placeholder for each call. Do not read a placeholder from a Kotlin `object`, and
 * do not use a MockK proxy on this path. Both alternatives read a static field before
 * [org.bukkit.Registry] completes initialisation.
 *
 * A Kotlin singleton can throw `NullPointerException` in this state. A MockK proxy can throw
 * `ExceptionInInitializerError`. A direct constructor call does not read the incomplete static state.
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
