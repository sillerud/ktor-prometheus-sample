package no.kevin.experiments

import io.ktor.server.netty.*
import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.server.engine.*
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Counter
import io.prometheus.client.exporter.common.TextFormat

val collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry
val isReadyCounter: Counter = Counter.build()
        .name("is_ready_counter").help("Counts requests to /is_ready").register()
val isAliveCounter: Counter = Counter.build()
        .name("is_alive_counter").help("Counts requests to /is_alive").register()

fun main(args: Array<String>) {
    embeddedServer(Netty, 8080) {
        routing {
            get("/is_ready") {
                isReadyCounter.inc()
                call.respondText("I'm ready")
            }
            get("/is_alive") {
                isAliveCounter.inc()
                call.respondText("I'm alive")
            }
            get("/prometheus") {
                val names = call.request.queryParameters.getAll("name[]")?.toSet() ?: setOf()
                call.respondWrite(ContentType.parse(TextFormat.CONTENT_TYPE_004)) {
                    TextFormat.write004(this, collectorRegistry.filteredMetricFamilySamples(names))
                }
            }
        }
    }.start(wait = true)
}
