package pw.jonak.typescriptserver

import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonException
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.post
import io.ktor.util.pipeline.PipelineContext

/** This module implements the TypeScript compile server API. */
fun Application.module() {
    val compiler = TSCompiler()

    install(DefaultHeaders)
    install(CallLogging)
    install(CORS) {
        // Make sure return information is receivable by the client.
        anyHost()
    }
    install(Routing) {
        post("/compile") {
            try {
                // JSON parsing
                val input = Klaxon().parse<TSProgram>(call.receiveText())
                if (input == null) {
                    inputError()
                    return@post
                }

                if (!input.verifySecure()) {
                    notSecure()
                    return@post
                }

                val output = compiler.compile(input)
                var html = AhaAdapter.convertToHtml(output.result)
                if (html == "") {
                    html = "<pre>${output.result}</pre>"
                }
                val htmlOutput = TSOutput(output.success, html, output.files)
                call.respondText(Klaxon().toJsonString(htmlOutput), ContentType.Application.Json)
            } catch (e: KlaxonException) {
                inputError()
            }
        }
    }
}

/** Lets the user know that their request did not conform to the expected structure. */
suspend inline fun <T : Any> PipelineContext<T, ApplicationCall>.inputError() {
    call.respondText(
        """{"error":true,"reason":"Could not parse input"}""",
        ContentType.Application.Json,
        HttpStatusCode.BadRequest
    )
}

/** Lets the user know that their request contained potentially dangerous pathnames. */
suspend inline fun <T : Any> PipelineContext<T, ApplicationCall>.notSecure() {
    call.respondText(
        """{"error":true,"reason":"File payload contained disallowed characters (.., /, or \)"}""",
        ContentType.Application.Json,
        HttpStatusCode.BadRequest
    )
}