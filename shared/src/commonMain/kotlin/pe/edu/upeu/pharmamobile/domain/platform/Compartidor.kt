package pe.edu.upeu.pharmamobile.domain.platform

/**
 * Capacidad nativa "compartir": el dominio solo sabe que existe. Cada
 * plataforma necesita un objeto propio para ejecutarla, por eso se resuelve
 * con una interfaz inyectada y no con expect/actual.
 */
interface Compartidor {
    fun compartir(texto: String)
}
