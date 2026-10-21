package pe.edu.upeu.pharmamobile.presentation.producto

private const val MINUTO = 60_000L
private const val HORA = 60 * MINUTO
private const val DIA = 24 * HORA

/**
 * Dice en lenguaje natural que tan viejos son los datos que se ven, por
 * ejemplo "Actualizado hace 2 horas". Recibe [ahora] en lugar de leer el
 * reloj para que sea una funcion pura y se pueda probar sin esperar.
 *
 * [ultima] es null cuando el telefono nunca sincronizo: se dice
 * explicitamente en lugar de mostrar una fecha vacia.
 */
fun describirAntiguedad(ultima: Long?, ahora: Long): String {
    if (ultima == null) return "Nunca sincronizado con PharmaSoft"
    // Un reloj del telefono atrasado daria una diferencia negativa: se trata como reciente.
    val transcurrido = (ahora - ultima).coerceAtLeast(0)
    return "Actualizado " + when {
        transcurrido < MINUTO -> "hace un momento"
        transcurrido < HORA -> hace(transcurrido / MINUTO, "minuto", "minutos")
        transcurrido < DIA -> hace(transcurrido / HORA, "hora", "horas")
        else -> hace(transcurrido / DIA, "día", "días")
    }
}

private fun hace(cantidad: Long, singular: String, plural: String) =
    "hace $cantidad ${if (cantidad == 1L) singular else plural}"
