package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.key.Key

/** The mutable builder behind one `advancements { ... }` block, keyed in declaration order. */
internal class SelectorAdvancementsBuilder : SelectorAdvancementsScope {
    val advancements: Map<Key, AdvancementCondition>
        field = mutableMapOf()

    override infix fun Key.eq(completed: Boolean) {
        put(this, AdvancementCondition.Completed(completed))
    }

    override infix fun Key.eq(criteria: AdvancementCriteriaScope.() -> Unit) {
        put(this, AdvancementCondition.Criteria(AdvancementCriteriaBuilder().apply(criteria).criteria))
    }

    private fun put(
        advancement: Key,
        condition: AdvancementCondition,
    ) {
        check(advancement !in advancements) {
            "Selector advancement '${advancement.asString()}' is already set; " +
                "vanilla syntax evaluates one condition per advancement."
        }
        advancements[advancement] = condition
    }
}
