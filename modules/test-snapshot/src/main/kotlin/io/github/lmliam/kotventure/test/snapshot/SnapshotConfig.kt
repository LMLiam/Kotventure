package io.github.lmliam.kotventure.test.snapshot

/**
 * Runtime configuration for [snapshot matching][matchSnapshot].
 *
 * The object reads each switch on each access and does not cache it. Thus, a CI job, Gradle invocation, or individual
 * test can change the behaviour. Each switch reads a JVM **system property first** and then an **environment variable**.
 * Use a system property for one Gradle run or test. Use an environment variable for CI.
 *
 * Intentionally `internal`: the public contract is the matcher plus the documented property/variable names, not this
 * object.
 */
internal object SnapshotConfig {
    /** System property that enables [updateMode]. It takes precedence over [UPDATE_ENV]. */
    const val UPDATE_PROPERTY: String = "kotventure.snapshot.update"

    /** Environment variable that enables [updateMode] when no system property is set. */
    const val UPDATE_ENV: String = "SNAPSHOT_UPDATE"

    /** System property that overrides [outputDir]. It takes precedence over [DIR_ENV]. */
    const val DIR_PROPERTY: String = "kotventure.snapshot.dir"

    /** Environment variable that overrides [outputDir] when no system property is set. */
    const val DIR_ENV: String = "SNAPSHOT_DIR"

    /**
     * Whether snapshots are recorded/updated instead of compared ("record mode").
     *
     * Enabled when the `kotventure.snapshot.update` system property or the `SNAPSHOT_UPDATE` environment variable is
     * `true`, `1`, or `yes` (case-insensitive). Disabled by default, so a mismatch never silently overwrites a
     * committed snapshot.
     */
    val updateMode: Boolean
        get() = setting(UPDATE_PROPERTY, UPDATE_ENV).toBooleanFlag()

    /**
     * Directory holding snapshot files, overriding the default test-resources location for non-standard layouts.
     *
     * Read from the `kotventure.snapshot.dir` system property or the `SNAPSHOT_DIR` environment variable. The value is
     * `null` when neither is set. When present, it is the read and write location. Files are directly at
     * `<dir>/<name>.snapshot.json`.
     */
    val outputDir: String?
        get() = setting(DIR_PROPERTY, DIR_ENV)?.takeIf { it.isNotBlank() }

    private fun setting(
        property: String,
        environment: String,
    ): String? = System.getProperty(property) ?: System.getenv(environment)

    private fun String?.toBooleanFlag(): Boolean = this?.trim()?.lowercase() in TRUE_VALUES

    private val TRUE_VALUES = setOf("true", "1", "yes")
}
