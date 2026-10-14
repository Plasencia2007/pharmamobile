package pe.edu.upeu.pharmamobile.data.local

/**
 * Almacén cifrado del token JWT. Es una interfaz y no un expect class
 * porque la implementación de Android necesita un Context en el constructor
 * y la de iOS no recibe nada (mismo criterio que Compartidor en la sesión 9).
 */
interface AlmacenSeguro {
    fun guardarToken(token: String)
    fun leerToken(): String?
    fun borrarToken()
}
