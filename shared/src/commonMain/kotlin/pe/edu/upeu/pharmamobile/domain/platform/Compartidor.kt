package pe.edu.upeu.pharmamobile.domain.platform

/**
 * Capacidad nativa "compartir": el dominio solo sabe que existe. Como en
 * cada plataforma necesita un objeto distinto (Context en Android, un
 * controlador de vista en iOS), se resuelve con interfaz + inyeccion y no
 * con expect/actual.
 */
interface Compartidor {
    fun compartir(texto: String)
}
