package io.github.lmliam.kotventure.core.selector

internal fun requireValidTeamName(team: String) {
    require(team.isNotEmpty()) {
        "Team name must not be empty; use team(none) to match no team"
    }
    require(team.all(Char::isAllowedInUnquotedSelectorToken)) {
        "Team name must use vanilla unquoted-token characters [0-9A-Za-z_.+-], got: $team"
    }
}
