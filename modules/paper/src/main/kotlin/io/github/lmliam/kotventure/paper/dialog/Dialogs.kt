@file:Suppress("UnstableApiUsage")

package io.github.lmliam.kotventure.paper.dialog

import io.github.lmliam.kotventure.paper.dialog.type.ConfirmationBuilder
import io.github.lmliam.kotventure.paper.dialog.type.ConfirmationScope
import io.github.lmliam.kotventure.paper.dialog.type.DialogListBuilder
import io.github.lmliam.kotventure.paper.dialog.type.DialogListScope
import io.github.lmliam.kotventure.paper.dialog.type.MultiActionBuilder
import io.github.lmliam.kotventure.paper.dialog.type.MultiActionScope
import io.github.lmliam.kotventure.paper.dialog.type.NoticeBuilder
import io.github.lmliam.kotventure.paper.dialog.type.NoticeScope
import io.github.lmliam.kotventure.paper.dialog.type.ServerLinksBuilder
import io.github.lmliam.kotventure.paper.dialog.type.ServerLinksScope
import io.papermc.paper.dialog.Dialog
import net.kyori.adventure.audience.Audience

/**
 * Selects a notice dialog with one optional acknowledgement button.
 *
 * @sample io.github.lmliam.kotventure.paper.dialog.showDialogSample
 */
public val notice: DialogKind<NoticeScope> =
    DialogKind { init -> NoticeBuilder(DialogBaseBuilder()).apply(init).build() }

/**
 * Selects a confirmation dialog with required confirmation and rejection buttons.
 *
 * @sample io.github.lmliam.kotventure.paper.dialog.dialogSample
 */
public val confirmation: DialogKind<ConfirmationScope> =
    DialogKind { init -> ConfirmationBuilder(DialogBaseBuilder()).apply(init).build() }

/**
 * Selects a dialog with one or more action buttons.
 *
 * The dialog can put the buttons in columns and can include an exit button.
 */
public val multiAction: DialogKind<MultiActionScope> =
    DialogKind { init -> MultiActionBuilder(DialogBaseBuilder()).apply(init).build() }

/**
 * Selects a dialog that shows a required set of [dialogs][DialogListScope.dialogs].
 */
public val dialogList: DialogKind<DialogListScope> =
    DialogKind { init -> DialogListBuilder(DialogBaseBuilder()).apply(init).build() }

/**
 * Selects a dialog that shows the server link list.
 *
 * You must set [columns][ServerLinksScope.columns] and [buttonWidth][ServerLinksScope.buttonWidth].
 */
public val serverLinks: DialogKind<ServerLinksScope> =
    DialogKind { init -> ServerLinksBuilder(DialogBaseBuilder()).apply(init).build() }

/**
 * Builds a [Dialog] of [kind] configured by [init].
 *
 * This function does not show or register the dialog. The returned value is independent of later
 * changes to the configuration block.
 *
 * @throws IllegalStateException when the required title is missing, a singleton slot is set
 *   twice, or the chosen kind's required configuration is incomplete.
 * @throws IllegalArgumentException when a configured value is outside its permitted range.
 * @sample io.github.lmliam.kotventure.paper.dialog.dialogSample
 */
public fun <S : DialogScope> dialog(
    kind: DialogKind<S>,
    init: S.() -> Unit,
): Dialog = kind.build(init)

/**
 * Builds a [Dialog] of [kind] configured by [init] and shows it to this audience.
 *
 * This function constructs the dialog before it calls [Audience.showDialog]. Therefore, invalid
 * configuration fails before the display call. Adventure does nothing if the audience cannot show
 * dialogs.
 *
 * @throws IllegalStateException when the required title is missing, a singleton slot is set
 *   twice, or the chosen kind's required configuration is incomplete.
 * @throws IllegalArgumentException when a configured value is outside its permitted range.
 * @sample io.github.lmliam.kotventure.paper.dialog.showDialogSample
 */
public fun <S : DialogScope> Audience.dialog(
    kind: DialogKind<S>,
    init: S.() -> Unit,
) {
    showDialog(kind.build(init))
}
