package io.github.lmliam.kotventure.build

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import java.net.URI
import java.security.MessageDigest
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicInteger

class BoundedHttpsDownloadTest :
    StringSpec(
        {
            "rejects a non-HTTPS source before opening a connection" {
                val opened = AtomicInteger()
                val operation = HttpConnectionOperation {
                    opened.incrementAndGet()
                    error("the connection must not open")
                }

                shouldThrow<IllegalArgumentException> {
                    BoundedHttpsDownload.download(
                        "http://fixture.test/file",
                        ByteArrayOutputStream(),
                        operation,
                        10,
                        3,
                        30_000,
                        120_000,
                    )
                }

                opened.get() shouldBe 0
            }

            "downloads a successful response and disconnects it" {
                withHttpServer { server ->
                    server.createContext("/fixture") { exchange ->
                        respond(exchange, 200, "fixture bytes")
                    }
                    server.start()
                    val opened = AtomicInteger()
                    val disconnected = AtomicInteger()
                    val operation = localOperation(server, opened, disconnected)
                    val output = ByteArrayOutputStream()

                    BoundedHttpsDownload.download(
                        logicalUrl(server, "/fixture"),
                        output,
                        operation,
                        100,
                        3,
                        30_000,
                        120_000,
                    )

                    output.toString(StandardCharsets.UTF_8) shouldBe "fixture bytes"
                    opened.get() shouldBe 1
                    disconnected.get() shouldBe 1
                }
            }

            "follows three redirects and rejects a fourth redirect" {
                withHttpServer { server ->
                    server.createContext("/") { exchange ->
                        val next = when (exchange.requestURI.path) {
                            "/start" -> "/one"
                            "/one" -> "/two"
                            "/two" -> "/final"
                            "/fourth-start" -> "/a"
                            "/a" -> "/b"
                            "/b" -> "/c"
                            "/c" -> "/d"
                            else -> null
                        }
                        if (next != null) {
                            exchange.responseHeaders.add("Location", next)
                            exchange.sendResponseHeaders(302, -1)
                            exchange.close()
                        } else {
                            respond(exchange, 200, "redirected")
                        }
                    }
                    server.start()
                    val opened = AtomicInteger()
                    val disconnected = AtomicInteger()
                    val operation = localOperation(server, opened, disconnected)
                    val output = ByteArrayOutputStream()

                    BoundedHttpsDownload.download(
                        logicalUrl(server, "/start"),
                        output,
                        operation,
                        100,
                        3,
                        30_000,
                        120_000,
                    )
                    output.toString(StandardCharsets.UTF_8) shouldBe "redirected"
                    opened.get() shouldBe 4
                    disconnected.get() shouldBe 4

                    shouldThrow<IOException> {
                        BoundedHttpsDownload.download(
                            logicalUrl(server, "/fourth-start"),
                            ByteArrayOutputStream(),
                            operation,
                            100,
                            3,
                            30_000,
                            120_000,
                        )
                    }
                    opened.get() shouldBe 8
                    disconnected.get() shouldBe 8
                }
            }

            "rejects an HTTPS downgrade and a missing redirect location" {
                withHttpServer { server ->
                    server.createContext("/downgrade") { exchange ->
                        exchange.responseHeaders.add("Location", "http://fixture.test/file")
                        exchange.sendResponseHeaders(302, -1)
                        exchange.close()
                    }
                    server.createContext("/missing") { exchange ->
                        exchange.sendResponseHeaders(302, -1)
                        exchange.close()
                    }
                    server.start()
                    val operation = localOperation(server, AtomicInteger(), AtomicInteger())

                    shouldThrow<IllegalArgumentException> {
                        BoundedHttpsDownload.download(
                            logicalUrl(server, "/downgrade"),
                            ByteArrayOutputStream(),
                            operation,
                            100,
                            3,
                            30_000,
                            120_000,
                        )
                    }
                    shouldThrow<IOException> {
                        BoundedHttpsDownload.download(
                            logicalUrl(server, "/missing"),
                            ByteArrayOutputStream(),
                            operation,
                            100,
                            3,
                            30_000,
                            120_000,
                        )
                    }
                }
            }

            "rejects non-success and unrecognised redirect statuses" {
                val notFound = connection(status = 404)
                shouldThrow<IOException> {
                    BoundedHttpsDownload.download("https://fixture.test/file", ByteArrayOutputStream(), { notFound }, 10, 3, 1, 2)
                }
                notFound.disconnected shouldBe 1

                val multipleChoices = connection(status = 300)
                shouldThrow<IOException> {
                    BoundedHttpsDownload.download("https://fixture.test/file", ByteArrayOutputStream(), { multipleChoices }, 10, 3, 1, 2)
                }
                multipleChoices.disconnected shouldBe 1

                val malformedLocation = connection(status = 302, location = "https://[")
                shouldThrow<IOException> {
                    BoundedHttpsDownload.download("https://fixture.test/file", ByteArrayOutputStream(), { malformedLocation }, 10, 3, 1, 2)
                }
                malformedLocation.disconnected shouldBe 1
            }

            "applies the byte limit when the length is absent or inaccurate" {
                val exact = connection(status = 200, body = "1234", contentLength = null)
                BoundedHttpsDownload.download("https://fixture.test/file", ByteArrayOutputStream(), { exact }, 4, 3, 1, 2)
                exact.disconnected shouldBe 1

                val oversized = connection(status = 200, body = "12345", contentLength = "1")
                shouldThrow<IOException> {
                    BoundedHttpsDownload.download("https://fixture.test/file", ByteArrayOutputStream(), { oversized }, 4, 3, 1, 2)
                }
                oversized.disconnected shouldBe 1

                val announcedOversize = connection(status = 200, body = "1", contentLength = "5")
                shouldThrow<IOException> {
                    BoundedHttpsDownload.download("https://fixture.test/file", ByteArrayOutputStream(), { announcedOversize }, 4, 3, 1, 2)
                }
                announcedOversize.inputRequested shouldBe 0
            }

            "sets both timeouts and disconnects after a read timeout" {
                val timeout = connection(inputFailure = SocketTimeoutException("test"))

                shouldThrow<SocketTimeoutException> {
                    BoundedHttpsDownload.download("https://fixture.test/file", ByteArrayOutputStream(), { timeout }, 10, 3, 31, 47)
                }

                timeout.connectTimeoutValue shouldBe 31
                timeout.readTimeoutValue shouldBe 47
                timeout.disconnected shouldBe 1
            }

            "updates all supplied digests while copying the response" {
                val sha1 = MessageDigest.getInstance("SHA-1")
                val sha256 = MessageDigest.getInstance("SHA-256")
                val output = ByteArrayOutputStream()
                val response = connection(body = "fixture bytes")

                BoundedHttpsDownload.download(
                    "https://fixture.test/file",
                    output,
                    { response },
                    100,
                    3,
                    1,
                    2,
                    sha1,
                    sha256,
                )

                output.toString(StandardCharsets.UTF_8) shouldBe "fixture bytes"
                sha1.digest().toHex() shouldBe "72f3d43d19a345ea7ceff688eb4cde9b323bf8e4"
                sha256.digest().toHex() shouldBe "06ef5892a96037b4eaac8d9b3dd5ee8765933a1f199b36cea53d2a163e091ae0"
            }
        },
    ) {
    class RecordingConnection(
        private val status: Int,
        private val body: String,
        private val contentLength: String?,
        private val inputFailure: IOException?,
        private val location: String?,
    ) : HttpConnection {
        var connectTimeoutValue = 0
        var readTimeoutValue = 0
        var disconnected = 0
        var inputRequested = 0

        override fun setFollowRedirects(followRedirects: Boolean) = Unit

        override fun setConnectTimeout(timeoutMillis: Int) {
            connectTimeoutValue = timeoutMillis
        }

        override fun setReadTimeout(timeoutMillis: Int) {
            readTimeoutValue = timeoutMillis
        }

        override fun responseCode(): Int = status

        override fun header(name: String): String? = when (name) {
            "Content-Length" -> contentLength
            "Location" -> location
            else -> null
        }

        override fun inputStream(): InputStream {
            inputRequested++
            inputFailure?.let { throw it }
            return ByteArrayInputStream(body.toByteArray())
        }

        override fun disconnect() {
            disconnected++
        }
    }
}

private fun connection(
    status: Int = 200,
    body: String = "",
    contentLength: String? = null,
    inputFailure: IOException? = null,
    location: String? = null,
) = BoundedHttpsDownloadTest.RecordingConnection(status, body, contentLength, inputFailure, location)

private fun withHttpServer(block: (HttpServer) -> Unit) {
    val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
    try {
        block(server)
    } finally {
        server.stop(0)
    }
}

private fun localOperation(
    server: HttpServer,
    opened: AtomicInteger,
    disconnected: AtomicInteger,
) = HttpConnectionOperation { logicalUri ->
    opened.incrementAndGet()
    val actualUri = URI(
        "http",
        null,
        server.address.hostString,
        server.address.port,
        logicalUri.path,
        logicalUri.query,
        null,
    )
    val connection = actualUri.toURL().openConnection() as java.net.HttpURLConnection
    object : HttpConnection {
        override fun setFollowRedirects(followRedirects: Boolean) {
            connection.instanceFollowRedirects = followRedirects
        }

        override fun setConnectTimeout(timeoutMillis: Int) {
            connection.connectTimeout = timeoutMillis
        }

        override fun setReadTimeout(timeoutMillis: Int) {
            connection.readTimeout = timeoutMillis
        }

        override fun responseCode(): Int = connection.responseCode

        override fun header(name: String): String? = connection.getHeaderField(name)

        override fun inputStream(): InputStream = connection.inputStream

        override fun disconnect() {
            connection.disconnect()
            disconnected.incrementAndGet()
        }
    }
}

private fun logicalUrl(server: HttpServer, path: String): String =
    "https://${server.address.hostString}:${server.address.port}$path"

private fun respond(exchange: HttpExchange, status: Int, body: String) {
    val bytes = body.toByteArray(StandardCharsets.UTF_8)
    exchange.sendResponseHeaders(status, bytes.size.toLong())
    exchange.responseBody.use { output -> output.write(bytes) }
}

private fun ByteArray.toHex(): String = joinToString("") { byte ->
    (byte.toInt() and 0xff).toString(16).padStart(2, '0')
}
