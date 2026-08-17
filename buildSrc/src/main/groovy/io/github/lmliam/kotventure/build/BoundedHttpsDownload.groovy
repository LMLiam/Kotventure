package io.github.lmliam.kotventure.build

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URISyntaxException
import java.security.MessageDigest

final class BoundedHttpsDownload {

    static final long MAXIMUM_BYTES = 128L * 1024L * 1024L
    static final int MAXIMUM_REDIRECTS = 3
    static final int CONNECT_TIMEOUT_MILLIS = 30_000
    static final int READ_TIMEOUT_MILLIS = 120_000
    private static final int BUFFER_SIZE = 8 * 1024
    private static final Set<Integer> REDIRECT_STATUSES = [301, 302, 303, 307, 308] as Set

    private static final HttpConnectionOperation DEFAULT_CONNECTION = new HttpConnectionOperation() {
        @Override
        HttpConnection open(URI uri) {
            def connection = uri.toURL().openConnection()
            if (!(connection instanceof HttpURLConnection)) {
                throw new IOException("Expected an HTTP connection for ${uri}")
            }

            return new HttpConnection() {
                @Override
                void setFollowRedirects(boolean followRedirects) {
                    connection.instanceFollowRedirects = followRedirects
                }

                @Override
                void setConnectTimeout(int timeoutMillis) {
                    connection.connectTimeout = timeoutMillis
                }

                @Override
                void setReadTimeout(int timeoutMillis) {
                    connection.readTimeout = timeoutMillis
                }

                @Override
                int responseCode() {
                    connection.responseCode
                }

                @Override
                String header(String name) {
                    connection.getHeaderField(name)
                }

                @Override
                InputStream inputStream() {
                    connection.inputStream
                }

                @Override
                void disconnect() {
                    connection.disconnect()
                }
            }
        }
    }

    private BoundedHttpsDownload() {
    }

    static URI requireHttpsUri(String sourceUrl) {
        URI source
        try {
            source = new URI(sourceUrl)
        } catch (URISyntaxException failure) {
            throw new IllegalArgumentException("sourceUrl must be a valid URI", failure)
        }

        if (source.scheme != 'https' || !source.host) {
            throw new IllegalArgumentException("sourceUrl must use an HTTPS URL")
        }

        source
    }

    static void download(String sourceUrl, OutputStream output) {
        download(
            sourceUrl,
            output,
            DEFAULT_CONNECTION,
            MAXIMUM_BYTES,
            MAXIMUM_REDIRECTS,
            CONNECT_TIMEOUT_MILLIS,
            READ_TIMEOUT_MILLIS,
        )
    }

    static void download(String sourceUrl, OutputStream output, MessageDigest... digests) {
        download(
            sourceUrl,
            output,
            DEFAULT_CONNECTION,
            MAXIMUM_BYTES,
            MAXIMUM_REDIRECTS,
            CONNECT_TIMEOUT_MILLIS,
            READ_TIMEOUT_MILLIS,
            digests,
        )
    }

    static void download(
        String sourceUrl,
        OutputStream output,
        HttpConnectionOperation connectionOperation,
        long maximumBytes,
        int maximumRedirects,
        int connectTimeoutMillis,
        int readTimeoutMillis,
        MessageDigest... digests
    ) {
        if (maximumBytes < 0) {
            throw new IllegalArgumentException('maximumBytes must not be negative')
        }
        if (maximumRedirects < 0) {
            throw new IllegalArgumentException('maximumRedirects must not be negative')
        }

        URI current = requireHttpsUri(sourceUrl)
        int redirects = 0

        while (true) {
            HttpConnection connection = null
            try {
                connection = connectionOperation.open(current)
                connection.setFollowRedirects(false)
                connection.setConnectTimeout(connectTimeoutMillis)
                connection.setReadTimeout(readTimeoutMillis)
                int responseCode = connection.responseCode()

                if (REDIRECT_STATUSES.contains(responseCode)) {
                    if (redirects >= maximumRedirects) {
                        throw new IOException("Too many redirects for ${sourceUrl}")
                    }
                    current = redirectTarget(current, connection.header('Location'))
                    redirects++
                    continue
                }

                if (responseCode < 200 || responseCode >= 300) {
                    throw new IOException("Unexpected HTTP status ${responseCode} for ${current}")
                }

                rejectOversizedContentLength(connection.header('Content-Length'), maximumBytes)
                copyBounded(connection.inputStream(), output, maximumBytes, digests)
                return
            } finally {
                if (connection != null) {
                    connection.disconnect()
                }
            }
        }
    }

    private static URI redirectTarget(URI current, String location) {
        if (!location) {
            throw new IOException("Redirect from ${current} has no Location header")
        }

        URI target
        try {
            target = current.resolve(new URI(location))
        } catch (URISyntaxException failure) {
            throw new IOException("Redirect from ${current} has an invalid Location header", failure)
        }

        requireHttpsUri(target.toString())
    }

    private static void rejectOversizedContentLength(String value, long maximumBytes) {
        if (!value) {
            return
        }

        long contentLength
        try {
            contentLength = Long.parseLong(value)
        } catch (NumberFormatException failure) {
            throw new IOException("Invalid Content-Length header: ${value}", failure)
        }

        if (contentLength < 0 || contentLength > maximumBytes) {
            throw new IOException("Content-Length exceeds the ${maximumBytes}-byte limit")
        }
    }

    private static void copyBounded(
        InputStream input,
        OutputStream output,
        long maximumBytes,
        MessageDigest[] digests
    ) {
        try {
            byte[] buffer = new byte[BUFFER_SIZE]
            long total = 0
            int count
            while ((count = input.read(buffer)) != -1) {
                if (count > maximumBytes - total) {
                    throw new IOException("Downloaded content exceeds the ${maximumBytes}-byte limit")
                }
                output.write(buffer, 0, count)
                digests.each { digest -> digest.update(buffer, 0, count) }
                total += count
            }
        } finally {
            input.close()
        }
    }
}
