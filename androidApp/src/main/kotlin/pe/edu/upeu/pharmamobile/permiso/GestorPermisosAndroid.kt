package pe.edu.upeu.pharmamobile.permiso

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import pe.edu.upeu.pharmamobile.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobile.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobile.domain.permiso.Permiso

class GestorPermisosAndroid(
    private val contexto: Context,
    private val preferencias: SharedPreferences
) : GestorPermisos {

    /** La Activity registra aqui su lanzador en onCreate. */
    var puente: PuentePermisos? = null

    /**
     * shouldShowRequestPermissionRationale devuelve falso en dos casos opuestos:
     * antes de la primera solicitud y tras la negativa definitiva. Para
     * distinguirlos se recuerda si el permiso ya se pidio alguna vez.
     */
    override suspend fun estado(permiso: Permiso): EstadoPermiso {
        val nombre = nombreDeSistema(permiso) ?: return EstadoPermiso.Concedido
        val concedido = ContextCompat.checkSelfPermission(contexto, nombre) ==
            PackageManager.PERMISSION_GRANTED
        if (concedido) return EstadoPermiso.Concedido
        val actividad = puente?.actividad ?: return EstadoPermiso.Denegado
        val yaSePidio = preferencias.getBoolean("pedido_$nombre", false)
        val procedeExplicar = ActivityCompat.shouldShowRequestPermissionRationale(actividad, nombre)
        return when {
            !yaSePidio -> EstadoPermiso.Denegado
            procedeExplicar -> EstadoPermiso.Denegado
            else -> EstadoPermiso.DenegadoParaSiempre
        }
    }

    override suspend fun solicitar(permiso: Permiso): EstadoPermiso {
        val nombre = nombreDeSistema(permiso) ?: return EstadoPermiso.Concedido
        preferencias.edit { putBoolean("pedido_$nombre", true) }
        val concedido = puente?.pedir(nombre) ?: false
        return if (concedido) EstadoPermiso.Concedido else estado(permiso)
    }

    override fun abrirAjustes() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", contexto.packageName, null)
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        contexto.startActivity(intent)
    }

    /** null = en esta version de Android no hace falta pedirlo. */
    private fun nombreDeSistema(permiso: Permiso): String? = when (permiso) {
        Permiso.CAMARA -> Manifest.permission.CAMERA
        Permiso.UBICACION -> Manifest.permission.ACCESS_COARSE_LOCATION
        Permiso.NOTIFICACIONES ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS else null
    }
}
