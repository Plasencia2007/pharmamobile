package pe.edu.upeu.pharmamobile.presentation.producto

import kotlin.test.Test
import kotlin.test.assertEquals

class AntiguedadDatosTest {

    private val ahora = 10_000_000_000L
    private val minuto = 60_000L
    private val hora = 60 * minuto
    private val dia = 24 * hora

    @Test
    fun `sin sincronizacion se dice explicitamente`() =
        assertEquals("Nunca sincronizado con PharmaSoft", describirAntiguedad(null, ahora))

    @Test
    fun `menos de un minuto es hace un momento`() =
        assertEquals("Actualizado hace un momento", describirAntiguedad(ahora - 30_000, ahora))

    @Test
    fun `minutos y horas en singular y plural`() {
        assertEquals("Actualizado hace 1 minuto", describirAntiguedad(ahora - minuto, ahora))
        assertEquals("Actualizado hace 45 minutos", describirAntiguedad(ahora - 45 * minuto, ahora))
        assertEquals("Actualizado hace 1 hora", describirAntiguedad(ahora - hora - 5 * minuto, ahora))
        assertEquals("Actualizado hace 2 horas", describirAntiguedad(ahora - 2 * hora, ahora))
    }

    @Test
    fun `mas de un dia se cuenta en dias`() {
        assertEquals("Actualizado hace 1 día", describirAntiguedad(ahora - 26 * hora, ahora))
        assertEquals("Actualizado hace 3 días", describirAntiguedad(ahora - 3 * dia, ahora))
    }

    @Test
    fun `un reloj atrasado no da un tiempo negativo`() =
        assertEquals("Actualizado hace un momento", describirAntiguedad(ahora + 5 * minuto, ahora))
}
