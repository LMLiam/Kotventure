package io.github.lmliam.kotventure.test.snapshot

/** Reads snapshot settings without caching them. System properties take precedence over environment variables. */
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
     * Reports whether record mode is active.
     *
     * The values `true`, `1`, and `yes` enable this mode without case sensitivity. The system
     * property takes precedence over the environment variable. The default is `false`.
     */
    val updateMode: Boolean
        get() = setting(UPDATE_PROPERTY, UPDATE_ENV).toBooleanFlag()

    /**
     * Returns the configured snapshot directory.
     *
     * The system property takes precedence over the environment variable. A blank or missing value
     * returns `null`. The snapshot APIs use this directory for reads and writes.
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
