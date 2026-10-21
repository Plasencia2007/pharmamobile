package pe.edu.upeu.pharmamobile.data.remote

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import pe.edu.upeu.pharmamobile.domain.model.Sesion
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Reconstruye la sesion SIN red leyendo la carga util del JWT (sub, authorities
 * y exp). El token esta firmado, no cifrado, asi que cualquiera puede leerla;
 * aqui solo sirve para abrir la app sin conexion con el usuario y los permisos
 * correctos. La firma NO se valida (la app no tiene la clave): el servidor sigue
 * validando cada peticion, por lo que un token alterado a mano solo cambiaria lo
 * que la interfaz ofrece, nunca lo que el servidor permite.
 *
 * Devuelve null si el token esta mal formado o ya vencio. Con [aceptarVencido]
 * un token vencido pero bien formado devuelve la sesion marcada como vencida:
 * es el caso "sin red y con el token caducado", donde mandar al login no sirve
 * porque no hay red por la que iniciar sesion.
 */
@OptIn(ExperimentalEncodingApi::class)
internal fun sesionDesdeToken(token: String, ahoraEnSegundos: Long, aceptarVencido: Boolean = false): Sesion? =
    try {
        val partes = token.split('.')
        require(partes.size == 3)
        val carga = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL)
            .decode(partes[1]).decodeToString()
        val json = Json.parseToJsonElement(carga).jsonObject
        val expira = json["exp"]?.jsonPrimitive?.longOrNull
        val usuario = json["sub"]?.jsonPrimitive?.content
        val vencido = expira != null && expira <= ahoraEnSegundos
        if (expira == null || usuario == null || (vencido && !aceptarVencido)) {
            null
        } else {
            val permisos = json["authorities"]?.jsonArray
                ?.map { it.jsonPrimitive.content }
                ?.filterNot { it.startsWith("ROLE_") }
                ?.toSet()
                .orEmpty()
            Sesion(token = token, usuario = usuario, nombreCompleto = usuario, permisos = permisos, vencida = vencido)
        }
    } catch (_: Exception) {
        null
    }
