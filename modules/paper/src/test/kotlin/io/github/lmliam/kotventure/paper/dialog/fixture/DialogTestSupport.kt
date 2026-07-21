package io.github.lmliam.kotventure.paper.dialog.fixture

import io.github.lmliam.kotventure.paper.dialog.DialogKind
import io.github.lmliam.kotventure.paper.dialog.DialogScope
import io.github.lmliam.kotventure.paper.dialog.dialog
import io.github.lmliam.kotventure.paper.dialog.notice
import io.papermc.paper.registry.data.dialog.DialogBase

internal fun <S : DialogScope> builtDialog(
    kind: DialogKind<S>,
    init: S.() -> Unit,
): FakeDialog = dialog(kind, init) as FakeDialog

internal fun builtDialog(init: DialogScope.() -> Unit): FakeDialog = builtDialog(notice, init)

internal fun builtBase(init: DialogScope.() -> Unit): DialogBase = builtDialog(init).base
