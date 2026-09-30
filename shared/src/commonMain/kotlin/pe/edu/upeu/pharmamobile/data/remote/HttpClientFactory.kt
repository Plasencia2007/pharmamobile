package pe.edu.upeu.pharmamobile.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.request.url
import kotlinx.serialization.json.Json

/**
 * URL base del backend PharmaSoft (https://github.com/dreyna/pharmaSoft),
 * levantado localmente con "mvnw spring-boot:run" (puerto 8082: el 8080 y
 * el 8081 ya los usan otros servicios en esta máquina). Cada
 * plataforma ve "localhost" distinto: el emulador de Android lo resuelve
 * como su propio loopback, no el de la máquina host, así que necesita la
 * IP especial 10.0.2.2; el simulador de iOS comparte la red del Mac y sí
 * puede usar localhost directamente.
 */
expect val urlBaseApi: String

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
        install(Logging) {
            // Logger.DEFAULT delega a SLF4J, que en Android cae a un logger
            // no-op sin un provider configurado y el log HTTP no se ve en
            // logcat. Logger.SIMPLE imprime por stdout (visible como tag
            // "System.out" en logcat) y funciona igual en Android e iOS.
            logger = Logger.SIMPLE
            level = LogLevel.HEADERS
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 10000
        }
        defaultRequest {
            url(urlBaseApi)
            contentType(ContentType.Application.Json)
        }
    }
