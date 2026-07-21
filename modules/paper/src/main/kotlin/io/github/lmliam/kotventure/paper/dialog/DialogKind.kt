package io.github.lmliam.kotventure.paper.dialog

import io.papermc.paper.dialog.Dialog

/**
 * Selects a dialog type and its configuration scope.
 *
 * Pass one of [notice], [confirmation], [multiAction], [dialogList], or [serverLinks] to [dialog].
 * The selected kind determines which type-specific functions are available in the block.
 *
 * This token is immutable. It does not construct or register a dialog until you pass it to [dialog].
 *
 * @param S the configuration scope for the selected dialog type.
 */
public class DialogKind<S : DialogScope> internal constructor(
    internal val build: (init: S.() -> Unit) -> Dialog,
)
