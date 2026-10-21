package pe.edu.upeu.pharmamobile.data.local

import app.cash.sqldelight.db.SqlDriver

/**
 * Quien abre el archivo de base de datos. El esquema y las consultas viven
 * en codigo comun; lo unico que cambia por plataforma es el driver. Es una
 * interfaz inyectada por la misma razon que AlmacenSeguro: la version de
 * Android necesita un Context y la de iOS no recibe nada.
 */
interface CreadorDeDriver {
    fun crear(): SqlDriver
}
