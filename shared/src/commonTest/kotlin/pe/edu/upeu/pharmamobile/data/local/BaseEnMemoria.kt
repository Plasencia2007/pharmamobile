package pe.edu.upeu.pharmamobile.data.local

import pe.edu.upeu.pharmamobile.db.PharmaDb

/**
 * Base SQLite en memoria para las pruebas, sin emulador y sin red. Cada
 * llamada crea una base NUEVA: si se compartiera, la primera prueba pasaria y
 * las siguientes fallarian por datos heredados. Cada plataforma de pruebas
 * aporta su driver (JDBC en la JVM, nativo en iOS).
 */
expect fun baseEnMemoria(): PharmaDb
