package pe.edu.upeu.pharmamobile.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.request.url
import kotlinx.serialization.json.Json

/** URL base de la API de práctica de la guía de la Sesión 7. */
const val URL_BASE_API = "https://api.escuelajs.co/api/v1/"

/**
 * Construye y configura el HttpClient una sola vez (Paso 3). Se registra
 * como single en Koin: el HttpClient es costoso de crear y no debe
 * instanciarse por cada petición.
 */
fun crearHttpClient(engine: HttpClientEngine): HttpClient =
    HttpClient(engine) {
        expectSuccess = true

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
        install(Logging) { level = LogLevel.HEADERS }
        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 10000
        }
        defaultRequest {
            url(URL_BASE_API)
            contentType(ContentType.Application.Json)
        }
    }
