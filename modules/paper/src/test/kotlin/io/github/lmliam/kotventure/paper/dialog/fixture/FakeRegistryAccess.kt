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
 * A ServiceLoader-registered [RegistryAccess] for unit tests that do not run a server.
 *
 * Dialog lookups return a placeholder [FakeDialog]. The data-component registry returns keyed placeholder component
 * types. This provider operates during the class initialisation of [org.bukkit.Registry]. Its `<clinit>` creates
 * legacy registries for all registry types, including [Dialog].
 *
 * Construct a new placeholder for each call. Do not read it from a Kotlin `object` or construct a MockK stub. Both
 * alternatives read a static field during their class initialisation. A singleton assigns its backing field only after
 * its class completes initialisation. A MockK proxy makes its target interface complete initialisation before Objenesis
 * constructs it.
 *
 * Either alternative enters the incomplete [Registry] `<clinit>` again and reads an unassigned field.
 * The singleton then throws `NullPointerException`, and MockK throws `ExceptionInInitializerError`. A direct constructor
 * call does not have this race.
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
