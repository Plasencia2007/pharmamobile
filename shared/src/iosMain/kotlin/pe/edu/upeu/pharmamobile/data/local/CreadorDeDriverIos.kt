package pe.edu.upeu.pharmamobile.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import pe.edu.upeu.pharmamobile.db.PharmaDb

/** NO PROBADO EN EJECUCION: se compila el modulo, pero no hay Mac para correrlo. */
class CreadorDeDriverIos : CreadorDeDriver {

    override fun crear(): SqlDriver = NativeSqliteDriver(
        schema = PharmaDb.Schema,
        name = "pharma.db"
    )
}
