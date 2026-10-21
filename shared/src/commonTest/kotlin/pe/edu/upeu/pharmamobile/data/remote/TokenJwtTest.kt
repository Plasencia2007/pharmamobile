package pe.edu.upeu.pharmamobile.data.remote

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalEncodingApi::class)
class TokenJwtTest {

    private fun token(carga: String): String {
        val b64 = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
        return "${b64.encode("""{"alg":"HS512"}""".encodeToByteArray())}.${b64.encode(carga.encodeToByteArray())}.firma"
    }

    @Test
    fun `un token vigente devuelve el usuario y sus permisos sin los roles`() {
        val t = token("""{"sub":"almacen","authorities":["CATALOGO_LEER","CATALOGO_ESCRIBIR","ROLE_ALMACENERO"],"iat":100,"exp":5000}""")

        val sesion = sesionDesdeToken(t, ahoraEnSegundos = 1000)

        assertEquals("almacen", sesion?.usuario)
        assertEquals(setOf("CATALOGO_LEER", "CATALOGO_ESCRIBIR"), sesion?.permisos)
        assertTrue(sesion!!.puede("CATALOGO_ESCRIBIR"))
    }

    @Test
    fun `un token vencido no abre sesion`() {
        val t = token("""{"sub":"admin","authorities":[],"exp":500}""")
        assertNull(sesionDesdeToken(t, ahoraEnSegundos = 1000))
    }

    @Test
    fun `sin red un token vencido abre la sesion marcada como vencida`() {
        val t = token("""{"sub":"admin","authorities":["CATALOGO_LEER"],"exp":500}""")

        val sesion = sesionDesdeToken(t, ahoraEnSegundos = 1000, aceptarVencido = true)

        assertEquals("admin", sesion?.usuario)
        assertTrue(sesion!!.vencida)
        assertTrue(sesion.puede("CATALOGO_LEER"))
    }

    @Test
    fun `un token vigente no se marca como vencido`() {
        val t = token("""{"sub":"admin","authorities":[],"exp":5000}""")
        assertEquals(false, sesionDesdeToken(t, ahoraEnSegundos = 1000, aceptarVencido = true)?.vencida)
    }

    @Test
    fun `un token mal formado no abre sesion`() {
        assertNull(sesionDesdeToken("esto.no.esuntoken", ahoraEnSegundos = 1000))
        assertNull(sesionDesdeToken("sin-puntos", ahoraEnSegundos = 1000))
        assertNull(sesionDesdeToken(token("""{"authorities":[],"exp":5000}"""), ahoraEnSegundos = 1000))
    }
}
