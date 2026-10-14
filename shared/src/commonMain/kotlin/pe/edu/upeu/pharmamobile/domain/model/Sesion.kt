package pe.edu.upeu.pharmamobile.domain.model

data class Credenciales(val usuario: String, val clave: String)

data class Sesion(
    val token: String,
    val usuario: String,
    val nombreCompleto: String,
    val permisos: Set<String>
) {
    fun puede(permiso: String): Boolean = permiso in permisos
}

/** Permisos que expone PharmaSoft; la UI los consulta con Sesion.puede(). */
object Permisos {
    const val CATALOGO_LEER = "CATALOGO_LEER"
    const val CATALOGO_ESCRIBIR = "CATALOGO_ESCRIBIR"
    const val CLIENTES_LEER = "CLIENTES_LEER"
    const val VENTAS_LEER = "VENTAS_LEER"
    const val VENTAS_REGISTRAR = "VENTAS_REGISTRAR"
}
