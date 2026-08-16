package io.github.lmliam.kotventure.build

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.channels.FileChannel
import java.nio.channels.Channels

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

import java.security.MessageDigest

/**
 * Downloads a file from a fixed URL and fails unless its pinned SHA-1 and SHA-256 match.
 *
 * - Retries up to 3 times with backoff.
 * - Writes to a ".part" temporary file in the destination directory and then atomically moves into place.
 * - Caching disabled because this task downloads an external fixture.
 */
@DisableCachingByDefault(because = "Downloads a checksum-pinned external fixture")
abstract class DownloadVerifiedFile extends DefaultTask {

    DownloadVerifiedFile() {
        outputs.upToDateWhen { false }
    }

    @Input
    abstract Property<String> getSourceUrl()

    @Input
    abstract Property<String> getExpectedSha1()

    @Input
    abstract Property<String> getExpectedSha256()

    @OutputFile
    abstract RegularFileProperty getDestination()

    private static final int ATTEMPTS = 3
    private static final int BUFFER_SIZE = 8 * 1024

    @TaskAction
    void download() {
        def src = sourceUrl.get()
        def expected = expectedSha1.getOrNull()
        def expectedSha256Value = expectedSha256.getOrNull()
        if (!src) {
            throw new GradleException("sourceUrl must be set")
        }
        if (!expected || !(expected ==~ /^[0-9a-f]{40}$/)) {
            throw new GradleException("expectedSha1 must be a 40-character hex SHA-1")
        }
        if (!expectedSha256Value || !(expectedSha256Value ==~ /^[0-9a-f]{64}$/)) {
            throw new GradleException("expectedSha256 must be a 64-character lower-case hex SHA-256")
        }
        BoundedHttpsDownload.requireHttpsUri(src)

        def target = destination.get().asFile.toPath()
        Files.createDirectories(target.parent)

        if (verifyExistingTarget(target, expected, expectedSha256Value)) {
            logger.lifecycle("Verified existing ${target}")
            return
        }

        Exception lastFailure = null
        for (int attempt = 1; attempt <= ATTEMPTS; attempt++) {
            Path temporary = null

            try {
                logger.lifecycle("Downloading ${src} (attempt $attempt)...")
                temporary = Files.createTempFile(target.parent, "${target.fileName}.", '.part')
                def sha1Digest = MessageDigest.getInstance('SHA-1')
                def sha256Digest = MessageDigest.getInstance('SHA-256')
                FileChannel.open(temporary, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING).withCloseable { outStream ->
                    BoundedHttpsDownload.download(
                        src,
                        Channels.newOutputStream(outStream),
                        sha1Digest,
                        sha256Digest,
                    )
                    outStream.force(true)
                }

                def actualSha1 = toHex(sha1Digest.digest())
                def actualSha256 = toHex(sha256Digest.digest())
                if (actualSha1 != expected || actualSha256 != expectedSha256Value) {
                    throw new GradleException(
                        "Checksum mismatch for ${src}: expected SHA-1 ${expected} and SHA-256 ${expectedSha256Value}, " +
                            "got SHA-1 ${actualSha1} and SHA-256 ${actualSha256}",
                    )
                }

                VerifiedFileReplacement.replace(temporary, target)

                logger.lifecycle("Downloaded and verified ${target}")
                return
            } catch (Exception e) {
                lastFailure = e
                logger.warn("Download attempt $attempt failed: ${e.class.simpleName}: ${e.message}")
                if (attempt < ATTEMPTS) {
                    sleep(attempt * 1000L)
                }
            } finally {
                if (temporary != null) {
                    Files.deleteIfExists(temporary)
                }
            }
        }

        throw new GradleException("Failed to download ${src} after ${ATTEMPTS} attempts.", lastFailure)
    }

    private static boolean verifyExistingTarget(Path target, String expectedSha1, String expectedSha256) {
        if (!Files.exists(target)) {
            return false
        }

        try {
            if (matchesExpectedDigests(target, expectedSha1, expectedSha256)) {
                return true
            }
        } catch (Exception failure) {
            throw new GradleException("Could not verify existing fixture ${target}.", failure)
        }

        Files.delete(target)
        return false
    }

    static boolean matchesExpectedDigests(Path path, String expectedSha1, String expectedSha256) {
        def digests = computeDigests(path)
        digests.sha1 == expectedSha1 && digests.sha256 == expectedSha256
    }

    private static Map<String, String> computeDigests(Path path) {
        MessageDigest sha1 = MessageDigest.getInstance('SHA-1')
        MessageDigest sha256 = MessageDigest.getInstance('SHA-256')
        Files.newInputStream(path).withCloseable { is ->
            byte[] buf = new byte[BUFFER_SIZE]
            int count
            while ((count = is.read(buf)) != -1) {
                sha1.update(buf, 0, count)
                sha256.update(buf, 0, count)
            }
        }
        [
            sha1: toHex(sha1.digest()),
            sha256: toHex(sha256.digest()),
        ]
    }

    private static String toHex(byte[] digest) {
        StringBuilder result = new StringBuilder(digest.length * 2)
        for (byte value : digest) {
            result.append(String.format('%02x', value & 0xff))
        }
        result.toString()
    }
}
