package io.github.lmliam.kotventure.paper.dialog.fixture

import io.mockk.every
import io.mockk.mockk
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.DialogInstancesProvider
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.action.DialogActionCallback
import io.papermc.paper.registry.data.dialog.body.ItemDialogBody
import io.papermc.paper.registry.data.dialog.body.PlainMessageDialogBody
import io.papermc.paper.registry.data.dialog.input.BooleanDialogInput
import io.papermc.paper.registry.data.dialog.input.NumberRangeDialogInput
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput
import io.papermc.paper.registry.data.dialog.input.TextDialogInput
import io.papermc.paper.registry.data.dialog.type.ConfirmationType
import io.papermc.paper.registry.data.dialog.type.DialogListType
import io.papermc.paper.registry.data.dialog.type.MultiActionType
import io.papermc.paper.registry.data.dialog.type.NoticeType
import io.papermc.paper.registry.data.dialog.type.ServerLinksType
import io.papermc.paper.registry.set.RegistrySet
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.api.BinaryTagHolder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.inventory.ItemStack

/**
 * Supplies Paper dialog instances to tests through `ServiceLoader`.
 *
 * Inline MockK stubs represent immutable results. Small fake builders record state across fluent
 * builder calls because ordinary stubs do not record these call sequences clearly.
 */
class FakeDialogInstancesProvider : DialogInstancesProvider {
    override fun dialogBaseBuilder(title: Component): DialogBase.Builder = FakeDialogBaseBuilder(title)

    override fun actionButtonBuilder(label: Component): ActionButton.Builder = FakeActionButtonBuilder(label)

    override fun register(
        callback: DialogActionCallback,
        options: ClickCallback.Options,
    ): DialogAction.CustomClickAction = FakeCallbackAction(callback, options)

    override fun staticAction(value: ClickEvent<*>): DialogAction.StaticAction =
        mockk { every { value() } returns value }

    override fun commandTemplate(template: String): DialogAction.CommandTemplateAction =
        mockk { every { template() } returns template }

    override fun customClick(
        id: Key,
        additions: BinaryTagHolder?,
    ): DialogAction.CustomClickAction =
        mockk {
            every { this@mockk.id() } returns id
            every { this@mockk.additions() } returns additions
        }

    override fun itemDialogBodyBuilder(itemStack: ItemStack): ItemDialogBody.Builder =
        FakeItemDialogBodyBuilder(itemStack)

    override fun plainMessageDialogBody(component: Component): PlainMessageDialogBody =
        plainMessageDialogBody(component, 200)

    override fun plainMessageDialogBody(
        component: Component,
        width: Int,
    ): PlainMessageDialogBody =
        mockk {
            every { contents() } returns component
            every { this@mockk.width() } returns width
        }

    override fun booleanBuilder(
        key: String,
        label: Component,
    ): BooleanDialogInput.Builder = FakeBooleanInputBuilder(key, label)

    override fun numberRangeBuilder(
        key: String,
        label: Component,
        start: Float,
        end: Float,
    ): NumberRangeDialogInput.Builder = FakeNumberRangeInputBuilder(key, label, start, end)

    override fun singleOptionBuilder(
        key: String,
        label: Component,
        entries: MutableList<SingleOptionDialogInput.OptionEntry>,
    ): SingleOptionDialogInput.Builder = FakeSingleOptionInputBuilder(key, label, entries.toList())

    override fun singleOptionEntry(
        id: String,
        display: Component?,
        initial: Boolean,
    ): SingleOptionDialogInput.OptionEntry =
        mockk {
            every { this@mockk.id() } returns id
            every { this@mockk.display() } returns display
            every { this@mockk.initial() } returns initial
        }

    override fun textBuilder(
        key: String,
        label: Component,
    ): TextDialogInput.Builder = FakeTextInputBuilder(key, label)

    override fun multilineOptions(
        maxLines: Int?,
        height: Int?,
    ): TextDialogInput.MultilineOptions =
        mockk {
            every { this@mockk.maxLines() } returns maxLines
            every { this@mockk.height() } returns height
        }

    override fun confirmation(
        yesButton: ActionButton,
        noButton: ActionButton,
    ): ConfirmationType =
        mockk {
            every { this@mockk.yesButton() } returns yesButton
            every { this@mockk.noButton() } returns noButton
        }

    override fun dialogList(dialogs: RegistrySet<Dialog>): DialogListType.Builder = FakeDialogListBuilder(dialogs)

    override fun multiAction(actions: MutableList<ActionButton>): MultiActionType.Builder =
        FakeMultiActionBuilder(actions.toList())

    override fun notice(): NoticeType =
        notice(
            mockk {
                every { label() } returns Component.text("OK")
                every { tooltip() } returns null
                every { this@mockk.width() } returns 150
                every { action() } returns null
            },
        )

    override fun notice(action: ActionButton): NoticeType = mockk { every { this@mockk.action() } returns action }

    override fun serverLinks(
        exitAction: ActionButton?,
        columns: Int,
        buttonWidth: Int,
    ): ServerLinksType =
        mockk {
            every { this@mockk.exitAction() } returns exitAction
            every { this@mockk.columns() } returns columns
            every { this@mockk.buttonWidth() } returns buttonWidth
        }
}
