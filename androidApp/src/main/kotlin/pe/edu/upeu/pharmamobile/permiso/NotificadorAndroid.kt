package pe.edu.upeu.pharmamobile.permiso

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import pe.edu.upeu.pharmamobile.MainActivity
import pe.edu.upeu.pharmamobile.R
import pe.edu.upeu.pharmamobile.domain.port.Notificador

private const val CANAL_STOCK = "stock_bajo"
private const val ID_STOCK = 1

class NotificadorAndroid(private val contexto: Context) : Notificador {

    // El sistema lo ignora en silencio si falta el permiso, y solo se llama con el permiso concedido.
    @SuppressLint("MissingPermission")
    override fun avisarStockBajo(productos: Int) {
        val gestor = NotificationManagerCompat.from(contexto)
        if (!gestor.areNotificationsEnabled()) return
        gestor.createNotificationChannel(
            NotificationChannel(CANAL_STOCK, "Stock bajo", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val abrirApp = PendingIntent.getActivity(
            contexto, 0,
            Intent(contexto, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        val texto = if (productos == 1) "1 producto llegó al stock mínimo." else "$productos productos llegaron al stock mínimo."
        val aviso = NotificationCompat.Builder(contexto, CANAL_STOCK)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Stock bajo en tu farmacia")
            .setContentText(texto)
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()
        gestor.notify(ID_STOCK, aviso)
    }
}
