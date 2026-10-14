package pe.edu.upeu.pharmamobile.di

import org.koin.core.module.Module

expect val platformModule: Module

/**
 * Nombre bajo el que cada plataforma registra un Boolean que indica si la
 * app es una compilación de depuración. Controla el nivel del Logging de Ktor:
 * apagado fuera de depuración para no dejar rastro de las peticiones.
 */
const val NOMBRE_DEPURACION = "depuracion"
