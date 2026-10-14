package pe.edu.upeu.pharmamobile.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpCallValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.encodedPath
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.request.url
import kotlinx.serialization.json.Json
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro

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

/** Ruta del login: es pública, por eso el token no viaja en esa petición. */
const val RUTA_LOGIN = "/auth/login"

/**
 * Construye y configura el HttpClient una sola vez. Se registra como single
 * en Koin: el HttpClient es costoso de crear y no debe instanciarse por
 * cada petición.
 *
 * Los plugins se instalan aquí y se aplican a TODAS las llamadas: ningún
 * repositorio escribe la cabecera Authorization a mano.
 *
 * @param alSesionExpirada se invoca cuando una ruta protegida responde 401
 *   (token vencido, manipulado o ausente); la app limpia el almacén y vuelve
 *   al login. Un 403 no lo invoca: es falta de permisos, no de sesión.
 */
fun crearHttpClient(
    engine: HttpClientEngine,
    almacen: AlmacenSeguro,
    esDepuracion: Boolean,
    alSesionExpirada: suspend () -> Unit = {}
): HttpClient =
    HttpClient(engine) {
        expectSuccess = true

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
        install(Auth) {
            bearer {
                // Se consulta una sola vez y queda en caché: tras un login o
                // un cierre de sesión hay que llamar a clearToken() (ver
                // AuthRepositorioRest) para que vuelva a leer el almacén.
                loadTokens { almacen.leerToken()?.let { BearerTokens(it, "") } }
                // El token viaja ya en la primera petición, salvo en el login:
                // evita el ciclo "sin token, 401, reintento" que duplicaría cada llamada.
                sendWithoutRequest { peticion -> !peticion.url.encodedPath.endsWith(RUTA_LOGIN) }
            }
        }
        install(Logging) {
            // Logger.DEFAULT delega a SLF4J, que en Android cae a un logger
            // no-op sin un provider configurado y el log HTTP no se ve en
            // logcat. Logger.SIMPLE imprime por stdout (visible como tag
            // "System.out" en logcat) y funciona igual en Android e iOS.
            logger = Logger.SIMPLE
            level = if (esDepuracion) LogLevel.HEADERS else LogLevel.NONE
            sanitizeHeader { nombre -> nombre == HttpHeaders.Authorization }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 10000
        }
        install(HttpCallValidator) {
            handleResponseExceptionWithRequest { excepcion, peticion ->
                val respuesta = (excepcion as? ClientRequestException)?.response ?: return@handleResponseExceptionWithRequest
                val esLogin = peticion.url.encodedPath.endsWith(RUTA_LOGIN)
                if (respuesta.status == HttpStatusCode.Unauthorized && !esLogin) alSesionExpirada()
            }
        }
        defaultRequest {
            url(urlBaseApi)
            contentType(ContentType.Application.Json)
        }
    }
