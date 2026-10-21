package pe.edu.upeu.pharmamobile.data.repository

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.remote.dto.PaginaResponseDto
import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoRequestDto
import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoResponseDto

// Piezas compartidas por las pruebas del repositorio offline (guia de la sesion 11
// y suite de la actividad autonoma 11): un almacen de token vacio y un PharmaSoft
// simulado con MockEngine, sin red real.

internal class AlmacenVacio : AlmacenSeguro {
    override fun guardarToken(token: String) = Unit
    override fun leerToken(): String? = null
    override fun borrarToken() = Unit
}

/** PharmaSoft simulado: registra el orden de las llamadas y puede "perder la red". */
internal class ServidorFalso {
    var conectado = true
    val llamadas = mutableListOf<String>()
    val productos = mutableListOf<ProductoResponseDto>()
    private var siguienteId = 100L
    private val json = headersOf(HttpHeaders.ContentType, "application/json")

    val motor = MockEngine { peticion ->
        if (!conectado) throw IOException("Sin red")
        val ruta = peticion.url.encodedPath
        when {
            peticion.method == HttpMethod.Post && ruta.endsWith("/productos") -> {
                llamadas += "POST"
                val cuerpo = Json.decodeFromString<ProductoRequestDto>((peticion.body as TextContent).text)
                val nuevo = ProductoResponseDto(siguienteId++, cuerpo.nombre, cuerpo.precio, cuerpo.stock, cuerpo.estado)
                productos += nuevo
                respond(Json.encodeToString(ProductoResponseDto.serializer(), nuevo), HttpStatusCode.Created, json)
            }
            peticion.method == HttpMethod.Get && ruta.endsWith("/productos") -> {
                llamadas += "GET"
                val pagina = PaginaResponseDto(productos.toList(), 0, 100, productos.size.toLong(), 1, true)
                respond(
                    Json.encodeToString(PaginaResponseDto.serializer(ProductoResponseDto.serializer()), pagina),
                    HttpStatusCode.OK, json
                )
            }
            else -> respondError(HttpStatusCode.NotFound)
        }
    }
}
