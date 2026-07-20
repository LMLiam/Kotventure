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
 * The notice dialog kind: an informational dialog with a single acknowledgement button.
 *
 * @sample io.github.lmliam.kotventure.paper.dialog.showDialogSample
 */
public val notice: DialogKind<NoticeScope> =
    DialogKind { init -> NoticeBuilder(DialogBaseBuilder()).apply(init).build() }

/**
 * The confirmation dialog kind: a yes/no dialog whose two buttons are both required.
 *
 * @sample io.github.lmliam.kotventure.paper.dialog.dialogSample
 */
public val confirmation: DialogKind<ConfirmationScope> =
    DialogKind { init -> ConfirmationBuilder(DialogBaseBuilder()).apply(init).build() }

/**
 * The multi-action dialog kind: one or more action buttons in an optional column layout with an
 * optional exit button.
 */
public val multiAction: DialogKind<MultiActionScope> =
    DialogKind { init -> MultiActionBuilder(DialogBaseBuilder()).apply(init).build() }

/**
 * The dialog-list dialog kind: a menu of entry buttons for a required set of
 * [dialogs][DialogListScope.dialogs].
 */
public val dialogList: DialogKind<DialogListScope> =
    DialogKind { init -> DialogListBuilder(DialogBaseBuilder()).apply(init).build() }

/**
 * The server-links dialog kind: the server's link list laid out in a required
 * [columns][ServerLinksScope.columns] count with buttons of a required
 * [buttonWidth][ServerLinksScope.buttonWidth].
 */
public val serverLinks: DialogKind<ServerLinksScope> =
    DialogKind { init -> ServerLinksBuilder(DialogBaseBuilder()).apply(init).build() }

/**
 * Builds a [Dialog] of [kind] configured by [init].
 *
 * This function only constructs the dialog and has no side effects. It does not show the dialog. Use it when a
 * dialog value is stored, reused, or passed to a later display call.
 *
 * @throws IllegalStateException when the required title is missing, a singleton slot is set
 *   twice, or the chosen kind's required configuration is incomplete.
 * @sample io.github.lmliam.kotventure.paper.dialog.dialogSample
 */
public fun <S : DialogScope> dialog(
    kind: DialogKind<S>,
    init: S.() -> Unit,
): Dialog = kind.build(init)

/**
 * Builds a [Dialog] of [kind] configured by [init] and shows it to this audience.
 *
 * Dialogs require a Minecraft 1.21.6+ server and client. On platforms or audiences that do not
 * support dialogs, Adventure's [Audience.showDialog] is a documented no-op.
 *
 * @throws IllegalStateException when the required title is missing, a singleton slot is set
 *   twice, or the chosen kind's required configuration is incomplete.
 * @sample io.github.lmliam.kotventure.paper.dialog.showDialogSample
 */
public fun <S : DialogScope> Audience.dialog(
    kind: DialogKind<S>,
    init: S.() -> Unit,
) {
    showDialog(kind.build(init))
}
