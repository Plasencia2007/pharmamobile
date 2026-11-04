package pe.edu.upeu.pharmamobile.data.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics
import pe.edu.upeu.pharmamobile.domain.port.Telemetria

/**
 * Unico lugar de la app que conoce el SDK de Firebase. Registrar un fallo nunca
 * debe causar otro: si Firebase no esta configurado (sin google-services.json,
 * o en iOS sin su plist) el aviso se pierde y la app sigue.
 */
class TelemetriaFirebase : Telemetria {

    override fun registrarError(e: Throwable, contexto: Map<String, String>) {
        runCatching {
            val crashlytics = Firebase.crashlytics
            contexto.forEach { (clave, valor) -> crashlytics.setCustomKey(clave, valor) }
            crashlytics.recordException(e)
        }
    }

    override fun registrarEvento(nombre: String, datos: Map<String, String>) {
        runCatching {
            Firebase.crashlytics.log(listOf(nombre, datos.entries.joinToString()).joinToString(" "))
        }
    }
}
