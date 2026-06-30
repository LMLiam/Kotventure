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
    if (key.isNotEmpty() && key.all { it.isLetterOrDigit() || it == '_' || it == '+' || it == '-' }) {
        key
    } else {
        "\"${escapeSnbtString(key)}\""
    }

internal fun renderValue(value: NbtValue): String =
    when (value) {
        is NbtValue.StringValue -> "\"${escapeSnbtString(value.value)}\""
        is NbtValue.ByteValue -> "${value.value}b"
        is NbtValue.ShortValue -> "${value.value}s"
        is NbtValue.IntValue -> "${value.value}"
        is NbtValue.LongValue -> "${value.value}L"
        is NbtValue.FloatValue -> "${value.value}f"
        is NbtValue.DoubleValue -> "${value.value}d"
        is NbtValue.CompoundValue -> renderCompound(value.compound)
        is NbtValue.ByteArrayValue -> value.values.joinToString(",", "[B;", "]") { "${it}b" }
        is NbtValue.IntArrayValue -> value.values.joinToString(",", "[I;", "]") { "$it" }
        is NbtValue.LongArrayValue -> value.values.joinToString(",", "[L;", "]") { "${it}L" }
    }

internal fun renderCompound(compound: NbtCompound): String =
    compound.entries.joinToString(",", "{", "}") { "${renderKey(it.key)}:${renderValue(it.value)}" }

internal fun renderNodes(nodes: List<NbtPathNode>): String =
    buildString {
        nodes.forEachIndexed { i, node ->
            when (node) {
                is NbtPathNode.Key -> {
                    if (i == 0) {
                        append(node.name)
                    } else {
                        append('.')
                        append(renderKey(node.name))
                    }
                }

                is NbtPathNode.Index -> append("[${node.index}]")
                is NbtPathNode.AllElements -> append("[]")
                is NbtPathNode.MatchingElements -> append("[${renderCompound(node.compound)}]")
            }
        }
    }
