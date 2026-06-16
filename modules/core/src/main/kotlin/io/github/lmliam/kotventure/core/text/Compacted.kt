package io.github.lmliam.kotventure.core.text

import net.kyori.adventure.text.Component

/**
 * Returns a normalised copy of this component tree, delegating to Adventure's [Component.compact].
 *
 * Compaction merges adjacent children that share the same style, pushes redundant parent styling down onto its
 * children, and drops empty wrapper components — all without changing how the tree renders. Reach for it before
 * serialising, snapshotting, or comparing components so that structurally different but visually identical trees
 * normalise to the same shape.
 *
 * This is a thin, past-tense alias of [Component.compact] (mirroring [styled][io.github.lmliam.kotventure.core.style.styled])
 * for call-site consistency with the rest of the DSL; it is functionally identical, so call [Component.compact]
 * directly if you prefer Adventure's own naming.
 */
public fun Component.compacted(): Component = compact()
