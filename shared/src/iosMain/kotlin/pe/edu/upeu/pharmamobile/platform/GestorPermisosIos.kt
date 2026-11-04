package pe.edu.upeu.pharmamobile.platform

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter
import pe.edu.upeu.pharmamobile.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobile.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobile.domain.permiso.Permiso
import pe.edu.upeu.pharmamobile.domain.port.Notificador
import kotlin.coroutines.resume

/**
 * NO PROBADO EN EJECUCION: se compila el modulo, pero no hay Mac para correrlo.
 * Solo las notificaciones estan resueltas: no necesitan clave en Info.plist.
 * Camara y ubicacion no se piden porque la app no las usa, y pedirlas sin su
 * clave de uso cerraria la aplicacion.
 */
class GestorPermisosIos : GestorPermisos {

    private val centro get() = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun estado(permiso: Permiso): EstadoPermiso {
        if (permiso != Permiso.NOTIFICACIONES) return EstadoPermiso.Denegado
        return suspendCancellableCoroutine { continuacion ->
            centro.getNotificationSettingsWithCompletionHandler { ajustes ->
                continuacion.resume(
                    when (ajustes?.authorizationStatus) {
                        UNAuthorizationStatusNotDetermined, null -> EstadoPermiso.Denegado
                        UNAuthorizationStatusDenied -> EstadoPermiso.DenegadoParaSiempre
                        else -> EstadoPermiso.Concedido
                    }
                )
            }
        }
    }

    override suspend fun solicitar(permiso: Permiso): EstadoPermiso {
        if (permiso != Permiso.NOTIFICACIONES) return EstadoPermiso.Denegado
        val opciones = UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound
        suspendCancellableCoroutine { continuacion ->
            centro.requestAuthorizationWithOptions(opciones) { _, _ -> continuacion.resume(Unit) }
        }
        return estado(permiso)
    }

    override fun abrirAjustes() {
        NSURL.URLWithString(UIApplicationOpenSettingsURLString)?.let {
            UIApplication.sharedApplication.openURL(it, options = emptyMap<Any?, Any>(), completionHandler = null)
        }
    }
}

/** NO PROBADO EN EJECUCION: se compila el modulo, pero no hay Mac para correrlo. */
class NotificadorIos : Notificador {
    override fun avisarStockBajo(productos: Int) {
        val contenido = UNMutableNotificationContent().apply {
            setTitle("Stock bajo en tu farmacia")
            setBody(if (productos == 1) "1 producto llegó al stock mínimo." else "$productos productos llegaron al stock mínimo.")
        }
        val solicitud = UNNotificationRequest.requestWithIdentifier("stock_bajo", contenido, null)
        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(solicitud, null)
    }
}
