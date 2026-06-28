package io.github.lmliam.kotventure.core.nbt

private fun escapeSnbtString(value: String): String =
    buildString(value.length) {
        value.forEach { ch ->
            when (ch) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\b' -> append("\\b")
                '\t' -> append("\\t")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                else -> if (ch.code < 0x20) append("\\u%04x".format(ch.code)) else append(ch)
            }
        }
    }

private fun renderKey(key: String): String =
    if (key.all { it.isLetterOrDigit() || it == '_' || it == '+' || it == '-' }) {
        key
    } else {
        "\"${escapeSnbtString(key)}\""
    }

internal fun renderLiteral(value: NbtLiteralValue): String =
    when (value) {
        is NbtLiteralValue.StringValue -> "\"${escapeSnbtString(value.value)}\""
        is NbtLiteralValue.ByteValue -> "${value.value}b"
        is NbtLiteralValue.ShortValue -> "${value.value}s"
        is NbtLiteralValue.IntValue -> "${value.value}"
        is NbtLiteralValue.LongValue -> "${value.value}L"
        is NbtLiteralValue.FloatValue -> "${value.value}f"
        is NbtLiteralValue.DoubleValue -> "${value.value}d"
        is NbtLiteralValue.CompoundValue -> renderPredicate(value.predicate)
        is NbtLiteralValue.ByteArrayValue -> value.values.joinToString(",", "[B;", "]") { "${it}b" }
        is NbtLiteralValue.IntArrayValue -> value.values.joinToString(",", "[I;", "]") { "$it" }
        is NbtLiteralValue.LongArrayValue -> value.values.joinToString(",", "[L;", "]") { "${it}L" }
    }

internal fun renderPredicate(predicate: NbtCompoundPredicate): String =
    predicate.entries.joinToString(",", "{", "}") { "${it.key}:${renderLiteral(it.value)}" }

internal fun renderNodes(nodes: List<NbtPathNode>): String =
    buildString {
        nodes.forEachIndexed { i, node ->
            when (node) {
                is NbtPathNode.RawRoot -> append(node.value)

                is NbtPathNode.Key -> {
                    if (i > 0) append('.')
                    append(renderKey(node.name))
                }

                is NbtPathNode.Index -> append("[${node.index}]")
                is NbtPathNode.AllElements -> append("[]")
                is NbtPathNode.MatchingElements -> append("[${renderPredicate(node.predicate)}]")
            }
        }
    }
