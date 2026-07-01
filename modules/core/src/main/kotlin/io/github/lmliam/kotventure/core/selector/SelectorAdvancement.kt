package io.github.lmliam.kotventure.core.selector

internal fun requireValidAdvancementCriterion(criterion: String) {
    require(criterion.isNotEmpty()) {
        "Advancement criterion must not be empty"
    }
    require(criterion.all(Char::isAllowedInUnquotedSelectorToken)) {
        "Advancement criterion must use vanilla unquoted-token characters [0-9A-Za-z_.+-], got: $criterion"
    }
}
