package pe.edu.upeu.pharmamobile.domain.repository

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.ResultadoEnvio

/** Lo que la sincronizacion necesita de la base local. */
interface ProductoLocalRepository {
    fun pendientes(maxIntentos: Int): List<Producto>
    fun confirmar(idLocal: Long, idServidor: Long)
    fun marcarFallida(id: Long, motivo: String, maxIntentos: Int)
    fun sumarIntento(id: Long, motivo: String)
    fun conciliar(productos: List<Producto>)
    fun contarPendientes(maxIntentos: Int): Int
    fun registrarSincronizacion(instante: Long)
}

/** Lo que la sincronizacion necesita del servidor, sin HTTP. */
interface ProductoRemotoRepository {
    suspend fun enviar(producto: Producto): ResultadoEnvio
    suspend fun listar(): List<Producto>
}
