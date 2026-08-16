package io.github.lmliam.kotventure.build

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.channels.FileChannel
import java.nio.ByteBuffer

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

    DownloadVerifiedFile() {
        outputs.upToDateWhen { false }
    }

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

        if (verifyExistingTarget(target, expected)) {
            logger.lifecycle("Verified existing ${target}")
            return
        }

        Exception lastFailure = null
        for (int attempt = 1; attempt <= ATTEMPTS; attempt++) {
            Path temporary = null

            try {
                logger.lifecycle("Downloading ${src} (attempt $attempt)...")
                temporary = Files.createTempFile(target.parent, "${target.fileName}.", '.part')
                URI uri = new URI(src)
                def conn = uri.toURL().openConnection()
                conn.connectTimeout = 30_000
                conn.readTimeout = 120_000

                conn.inputStream.withCloseable { inStream ->
                    FileChannel.open(temporary, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING).withCloseable { outStream ->
                        byte[] buf = new byte[BUFFER_SIZE]
                        int r
                        while ((r = inStream.read(buf)) != -1) {
                            ByteBuffer buffer = ByteBuffer.wrap(buf, 0, r)
                            while (buffer.hasRemaining()) {
                                outStream.write(buffer)
                            }
                        }
                        outStream.force(true)
                    }
                }

                def actualSha1 = computeSha1Hex(temporary)
                if (actualSha1 != expected) {
                    throw new GradleException("Checksum mismatch for ${src}: expected ${expected}, got ${actualSha1}")
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

    private static boolean verifyExistingTarget(Path target, String expected) {
        if (!Files.exists(target)) {
            return false
        }

        try {
            if (computeSha1Hex(target) == expected) {
                return true
            }
        } catch (Exception failure) {
            throw new GradleException("Could not verify existing fixture ${target}.", failure)
        }

        Files.delete(target)
        return false
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
        StringBuilder sb = new StringBuilder(digest.length * 2)
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff))
        }
        return sb.toString()
    }
}
