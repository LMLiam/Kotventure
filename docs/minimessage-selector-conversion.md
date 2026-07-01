# MiniMessage selector conversion

`miniToDsl(...)` uses the structured selector parser from `core` when it encounters an Adventure
selector component. A pattern that the typed selector DSL can reproduce exactly is emitted as its
matching factory and scoped arguments:

```kotlin
miniToDsl("<selector:'@e[type=!minecraft:zombie,limit=2]'>")
```

produces:

```kotlin
component {
    selector(
        entities {
            not {
                type(key("minecraft", "zombie"))
            }
            limit(2)
        }
    )
}
```

The converter supports all six selector heads and the complete first-wave argument surface,
including origin and volume groups, ranges, presence values, NBT, scores, predicates,
advancements, and nested `not` blocks. Generated keys use the existing Adventure `Key` bridge,
and generated SNBT uses the existing typed NBT DSL.

## Lossless fallback

Typed output is used only when running that output would preserve the selector pattern exactly.
The converter emits `entitySelector("...")` instead when:

- the `core` parser rejects unknown or future syntax;
- a lexical form would be normalized, such as a bare entity key or non-canonical number;
- argument ordering or repetition would be collapsed by a typed builder;
- positive and negated forms cannot coexist in the same typed builder state;
- an explicit empty argument list must be preserved; or
- an SNBT value has no typed NBT representation.

For example, the bare key in this pattern must remain byte-for-byte intact:

```kotlin
component {
    selector(entitySelector("@e[type=armor_stand,limit=1]"))
}
```

Both paths generate compiling Kotlin and preserve the Adventure selector pattern, separator,
style, and children.
