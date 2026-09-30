package pe.edu.upeu.pharmamobile.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import pe.edu.upeu.pharmamobile.data.remote.dto.PaginaDto
import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoDto

class ProductoApi(private val client: HttpClient) {

    suspend fun obtenerProductos(pagina: Int = 0, tamanio: Int = 20): PaginaDto<ProductoDto> =
        client.get("productos") {
            parameter("pagina", pagina)
            parameter("tamanio", tamanio)
        }.body()
}
