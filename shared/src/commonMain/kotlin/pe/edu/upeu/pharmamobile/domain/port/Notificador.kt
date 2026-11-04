package pe.edu.upeu.pharmamobile.domain.port

/** Aviso del sistema al usuario. Cada plataforma lo muestra a su manera. */
interface Notificador {
    fun avisarStockBajo(productos: Int)
}
