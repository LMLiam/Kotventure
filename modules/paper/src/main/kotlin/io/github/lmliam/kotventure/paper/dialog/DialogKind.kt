package io.github.lmliam.kotventure.paper.dialog

import io.papermc.paper.dialog.Dialog

/**
 * A compile-time token selecting one dialog type. Passed to [dialog] (or
 * [Audience.dialog][io.github.lmliam.kotventure.paper.dialog.dialog]), its type parameter [S]
 * fixes which scope the configuration block receives, so the chosen kind's own capabilities — and
 * only those — are in scope.
 *
 * Obtain a kind from the tokens in this package: [notice], [confirmation], [multiAction],
 * [dialogList], and [serverLinks].
 */
public class DialogKind<S : DialogScope> internal constructor(
    internal val build: (init: S.() -> Unit) -> Dialog,
)
