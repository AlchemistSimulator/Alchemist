/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.acquisition

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.TreeMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * A request captured by [FakeHttpServer].
 */
internal class CapturedRequest(
    val method: String,
    val route: String,
    val body: String,
    private val headers: Map<String, String>,
) {
    fun header(name: String): String? = headers[name]
}

/**
 * A function that takes an [HttpExchange] and writes a response into it.
 */
internal typealias Responder = (HttpExchange) -> Unit

/**
 * A dummy in-JVM HTTP server for testing REST providers without external dependencies.
 *
 * A single handler intercepts **all** requests; responses are configured via
 * [enqueue] to simulate states (e.g., polling) or [constant] for static endpoints.
 *
 * Each received request is logged in [requests] for test assertions.
 */
internal class FakeHttpServer : AutoCloseable {

    /**
     * An HTTP server. Listen for requests on this machine on a random available port.
     */
    private val server: HttpServer = HttpServer.create(
        InetSocketAddress("127.0.0.1", 0),
        0,
    ).apply {
        // runs on a single thread
        executor = Executors.newSingleThreadExecutor()
    }

    /**
     * Associates each method-route with a queue of responses for that pair.
     * Used to simulate responses that change over time (e.g. on polling).
     */
    private val queues = HashMap<Pair<String, String>, ArrayDeque<Responder>>()

    /**
     * Associates each method-route with a constant response for that pair.
     */
    private val constants = HashMap<Pair<String, String>, Responder>()

    // all requests received, in arrival order (for test assertions).
    val requests = mutableListOf<CapturedRequest>()

    // base URL to feed into the provider's endpoint.
    val baseUrl: String get() = "http://127.0.0.1:${server.address.port}"

    init {
        // a single handler intercepts all requests.
        server.createContext("/") { exchange ->
            exchange.use { ex ->
                // reads the full request body
                val body = ex.requestBody.readBytes().toString(Charsets.UTF_8)

                // each header can appear multiple times: takes only the first one of each kind
                val headers = TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER).apply {
                    ex.requestHeaders.forEach { (name, values) -> put(name, values.firstOrNull().orEmpty()) }
                }
                requests += CapturedRequest(ex.requestMethod, ex.requestURI.path, body, headers)

                val key = ex.requestMethod to ex.requestURI.path
                // searches in the queues first (consuming the key), fallback on constant responses otherwise
                val responder = queues[key]?.removeFirstOrNull() ?: constants[key]
                // executes the responder if it exists, defaults to a 404 otherwise
                if (responder != null) responder(ex) else respond(ex, 404, ByteArray(0))
            }
        }
        server.start()
    }

    /**
     * Enqueues a response for the next [method]-[route] request.
     *
     * @param method the HTTP method used.
     * @param route the requested route.
     * @param responder the response for the next [method]-[route] request.
     */
    fun enqueue(method: String, route: String, responder: Responder) {
        queues.getOrPut(method to route) { ArrayDeque() }.addLast(responder)
    }

    /**
     * Registers a fallback [responder] for [method]-[route],
     * used whenever its queue is empty.
     *
     * @param method the HTTP method used.
     * @param route the queried route.
     * @param responder responder the response used for every [method]-[route]
     * request once the queue is empty.
     */
    fun constant(method: String, route: String, responder: Responder) {
        constants[method to route] = responder
    }

    /**
     * Stops the server AND shuts down its executor.
     */
    override fun close() {
        server.stop(0)
        (server.executor as? ExecutorService)?.shutdownNow()
    }

    companion object {
        /**
         * Returns a responder that writes a JSON string as
         * a response.
         *
         * @param code HTTP response code.
         * @param body the JSON body as a string.
         */
        fun json(code: Int, body: String): Responder = { ex ->
            ex.responseHeaders.add("Content-Type", "application/json")
            respond(ex, code, body.toByteArray())
        }

        /**
         * Simulates a binary response (e.g. a downloadable file).
         *
         * @param code HTTP response code.
         * @param data the raw data sent in the response.
         */
        fun bytes(code: Int, data: ByteArray): Responder = { ex -> respond(ex, code, data) }

        /**
         * Adds a response in the [ex] http exchange.
         *
         * @param ex the HTTP exchange.
         * @param code the HTTP status code of the response.
         * @param data the raw data sent in the response.
         */
        private fun respond(ex: HttpExchange, code: Int, data: ByteArray) {
            ex.sendResponseHeaders(
                code,
                // -1 means that there is not a body
                if (data.isEmpty()) -1 else data.size.toLong(),
            )
            if (data.isNotEmpty()) ex.responseBody.use { it.write(data) }
        }
    }
}
