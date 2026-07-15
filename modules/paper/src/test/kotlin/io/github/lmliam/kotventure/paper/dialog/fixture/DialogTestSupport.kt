package io.github.lmliam.kotventure.paper.dialog.fixture

import io.github.lmliam.kotventure.paper.dialog.DialogKind
import io.github.lmliam.kotventure.paper.dialog.DialogScope
import io.github.lmliam.kotventure.paper.dialog.dialog
import io.github.lmliam.kotventure.paper.dialog.notice
import io.papermc.paper.registry.data.dialog.DialogBase

/** Builds a dialog of [kind] through the DSL and narrows to the recording [FakeDialog]. */
internal fun <S : DialogScope> builtDialog(
    kind: DialogKind<S>,
    init: S.() -> Unit,
): FakeDialog = dialog(kind, init) as FakeDialog

/** Builds a notice dialog and narrows to the recording [FakeDialog] for base/type assertions. */
internal fun builtDialog(init: DialogScope.() -> Unit): FakeDialog = builtDialog(notice, init)

/** Builds a notice dialog through the DSL and returns its recorded [DialogBase]. */
internal fun builtBase(init: DialogScope.() -> Unit): DialogBase = builtDialog(init).base
