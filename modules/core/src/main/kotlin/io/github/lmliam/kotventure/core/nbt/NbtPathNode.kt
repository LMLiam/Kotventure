package io.github.lmliam.kotventure.core.nbt

internal sealed interface NbtPathNode {
    data class Key(
        val name: String,
    ) : NbtPathNode

    data class Index(
        val index: Int,
    ) : NbtPathNode

    data object AllElements : NbtPathNode

    data class MatchingElements(
        val predicate: NbtCompoundPredicate,
    ) : NbtPathNode
}
