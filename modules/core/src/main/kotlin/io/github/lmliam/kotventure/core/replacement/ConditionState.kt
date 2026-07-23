package io.github.lmliam.kotventure.core.replacement

internal class ConditionState(
    override val match: TextMatch,
    override val matchCount: Int,
    override val replacementCount: Int,
) : ConditionScope {
    override val replace: MatchAction get() = MatchAction.REPLACE
    override val skip: MatchAction get() = MatchAction.SKIP
    override val stop: MatchAction get() = MatchAction.STOP
}
