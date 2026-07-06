import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Downloads a file from a fixed URL and fails unless its SHA-1 matches the pinned checksum.
 */
@DisableCachingByDefault(because = 'Downloads a checksum-pinned external fixture')
abstract class DownloadVerifiedFile extends DefaultTask {
    @Input
    abstract Property<String> getSourceUrl()

    @Input
    abstract Property<String> getExpectedSha1()

    @OutputFile
    abstract RegularFileProperty getDestination()

    @TaskAction
    void download() {
        def target = destination.get().asFile
        target.parentFile.mkdirs()
        def temporary = new File(target.parentFile, "${target.name}.part")
        Exception lastFailure = null

        for (int attempt = 1; attempt <= 3; attempt++) {
            temporary.delete()
            try {
                def connection = new URI(sourceUrl.get()).toURL().openConnection()
                connection.connectTimeout = 30_000
                connection.readTimeout = 120_000
                connection.inputStream.withCloseable { input ->
                    temporary.withOutputStream { output -> output << input }
                }
                def actualSha1 = sha1(temporary)
                if (actualSha1 != expectedSha1.get()) {
                    throw new GradleException(
                        "Checksum mismatch for ${sourceUrl.get()}: " +
                            "expected ${expectedSha1.get()}, got $actualSha1",
                    )
                }
                Files.move(
                    temporary.toPath(),
                    target.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                )
                return
            } catch (Exception failure) {
                lastFailure = failure
                temporary.delete()
                if (attempt < 3) {
                    logger.warn("Download attempt $attempt failed; retrying: ${failure.message}")
                    Thread.sleep(attempt * 1_000L)
                }
            }
        }

        throw new GradleException("Failed to download ${sourceUrl.get()} after 3 attempts.", lastFailure)
    }

    private static String sha1(File file) {
        def digest = MessageDigest.getInstance('SHA-1')
        file.withInputStream { input ->
            byte[] buffer = new byte[8192]
            int read
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read)
            }
        }
        digest.digest().encodeHex().toString()
    }
}
