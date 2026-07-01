package io.github.lmliam.kotventure.core.selector

internal fun requireValidScoreObjective(objective: String) {
    require(objective.isNotEmpty()) {
        "Score objective must not be empty"
    }
    require(objective.all(Char::isAllowedInUnquotedSelectorToken)) {
        "Score objective must use vanilla unquoted-token characters [0-9A-Za-z_.+-], got: $objective"
    }
}
