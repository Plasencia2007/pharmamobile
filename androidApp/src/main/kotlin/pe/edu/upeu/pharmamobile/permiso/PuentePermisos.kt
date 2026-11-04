package pe.edu.upeu.pharmamobile.permiso

import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.CompletableDeferred

/**
 * Conecta la corrutina con el lanzador de la Activity: convierte la respuesta
 * del dialogo del sistema en el valor que la funcion de suspension espera. Se
 * crea en onCreate porque el lanzador debe registrarse antes de que la
 * Activity arranque.
 */
class PuentePermisos(val actividad: ComponentActivity) {
    private var pendiente: CompletableDeferred<Boolean>? = null
    private val lanzador = actividad.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido -> pendiente?.complete(concedido) }

    suspend fun pedir(nombre: String): Boolean {
        val espera = CompletableDeferred<Boolean>()
        pendiente = espera
        lanzador.launch(nombre)
        return espera.await()
    }

    /** Si la Activity se destruye con el dialogo abierto, nadie responderia a la espera. */
    fun liberar() {
        pendiente?.complete(false)
    }
}
