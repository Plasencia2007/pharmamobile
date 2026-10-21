package pe.edu.upeu.pharmamobile.data.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import pe.edu.upeu.pharmamobile.db.PharmaDb

actual fun baseEnMemoria(): PharmaDb {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    PharmaDb.Schema.create(driver)
    return PharmaDb(driver)
}
