import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

import java.security.DigestInputStream
import java.security.MessageDigest

/**
 * Downloads a file from a fixed URL and fails unless its SHA-1 matches the pinned checksum.
 *
 * - Retries up to 3 times with backoff.
 * - Writes to a ".part" temporary file in the destination directory and then atomically moves into place.
 * - Caching disabled because this task downloads an external fixture.
 */
@DisableCachingByDefault(because = "Downloads a checksum-pinned external fixture")
abstract class DownloadVerifiedFile extends DefaultTask {

    @Input
    abstract Property<String> getSourceUrl()

    @Input
    abstract Property<String> getExpectedSha1()

    @OutputFile
    abstract RegularFileProperty getDestination()

    private static final int ATTEMPTS = 3
    private static final int BUFFER_SIZE = 8 * 1024

    @TaskAction
    void download() {
        // Validate early
        def src = sourceUrl.get()
        def expected = expectedSha1.get()?.toLowerCase()?.trim()
        if (!src) {
            throw new GradleException("sourceUrl must be set")
        }
        if (!expected || !(expected ==~ /^[0-9a-f]{40}$/)) {
            throw new GradleException("expectedSha1 must be a 40-character hex SHA-1")
        }

        def target = destination.get().asFile.toPath()
        Files.createDirectories(target.parent)

        def temporary = target.resolveSibling("${target.fileName}.part")

        Exception lastFailure = null
        for (int attempt = 1; attempt <= ATTEMPTS; attempt++) {
            // ensure clean temporary file
            try {
                Files.deleteIfExists(temporary)
            } catch (Exception ignored) {
            }

            try {
                logger.lifecycle("Downloading ${src} (attempt $attempt)...")
                URI uri = new URI(src)
                def conn = uri.toURL().openConnection()
                conn.connectTimeout = 30_000
                conn.readTimeout = 120_000

                // Stream download to temporary file
                conn.inputStream.withCloseable { inStream ->
                    // Open temporary for writing (replace if exists)
                    Files.newOutputStream(temporary, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).withCloseable { outStream ->
                        byte[] buf = new byte[BUFFER_SIZE]
                        int r
                        while ((r = inStream.read(buf)) != -1) {
                            outStream.write(buf, 0, r)
                        }
                    }
                }

                // Compute SHA-1 from temporary file (streaming)
                def actualSha1 = computeSha1Hex(temporary)
                if (actualSha1 != expected) {
                    throw new GradleException("Checksum mismatch for ${src}: expected ${expected}, got ${actualSha1}")
                }

                // Move into place (attempt atomic move, fall back to replace)
                try {
                    Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
                } catch (UnsupportedOperationException ignored) {
                    Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING)
                }

                logger.lifecycle("Downloaded and verified ${target}")
                return
            } catch (Exception e) {
                lastFailure = e
                logger.warn("Download attempt $attempt failed: ${e.class.simpleName}: ${e.message}")
                try {
                    Files.deleteIfExists(temporary)
                } catch (Exception ignored) {
                }
                if (attempt < ATTEMPTS) {
                    // simple backoff
                    sleep(attempt * 1000L)
                }
            }
        }

        throw new GradleException("Failed to download ${src} after ${ATTEMPTS} attempts.", lastFailure)
    }

    private static String computeSha1Hex(Path path) {
        MessageDigest md = MessageDigest.getInstance("SHA-1")
        Files.newInputStream(path).withCloseable { is ->
            new DigestInputStream(is, md).withCloseable { dis ->
                byte[] buf = new byte[BUFFER_SIZE]
                while (dis.read(buf) != -1) {
                    // DigestInputStream updates the digest for us
                }
            }
        }
        byte[] digest = md.digest()
        // convert to lower-case hex
        StringBuilder sb = new StringBuilder(digest.length * 2)
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff))
        }
        return sb.toString()
    }
}
