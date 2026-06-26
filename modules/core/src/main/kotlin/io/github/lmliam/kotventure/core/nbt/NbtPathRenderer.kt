package io.github.lmliam.kotventure.core.nbt

internal fun renderLiteral(value: NbtLiteralValue): String =
    when (value) {
        is NbtLiteralValue.StringValue -> "\"${value.value}\""
        is NbtLiteralValue.ByteValue -> "${value.value}b"
        is NbtLiteralValue.ShortValue -> "${value.value}s"
        is NbtLiteralValue.IntValue -> "${value.value}"
        is NbtLiteralValue.LongValue -> "${value.value}L"
        is NbtLiteralValue.FloatValue -> "${value.value}f"
        is NbtLiteralValue.DoubleValue -> "${value.value}d"
        is NbtLiteralValue.ByteArrayValue -> value.values.joinToString(",", "[B;", "]") { "${it}b" }
        is NbtLiteralValue.IntArrayValue -> value.values.joinToString(",", "[I;", "]") { "$it" }
        is NbtLiteralValue.LongArrayValue -> value.values.joinToString(",", "[L;", "]") { "${it}L" }
        is NbtLiteralValue.CompoundValue -> renderPredicate(value.predicate)
        is NbtLiteralValue.ListValue -> value.elements.joinToString(",", "[", "]") { renderLiteral(it) }
    }

internal fun renderPredicate(predicate: NbtCompoundPredicate): String =
    predicate.entries.joinToString(",", "{", "}") { "${it.key}:${renderLiteral(it.value)}" }

internal fun renderNodes(nodes: List<NbtPathNode>): String =
    buildString {
        nodes.forEachIndexed { i, node ->
            when (node) {
                is NbtPathNode.Key -> {
                    if (i > 0) append('.')
                    append(node.name)
                }
                is NbtPathNode.Index -> append("[${node.index}]")
                is NbtPathNode.AllElements -> append("[]")
                is NbtPathNode.MatchingElements -> append("[${renderPredicate(node.predicate)}]")
            }
        }
    }
