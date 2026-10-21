package pe.edu.upeu.pharmamobile.data.local

import app.cash.sqldelight.driver.native.inMemoryDriver
import pe.edu.upeu.pharmamobile.db.PharmaDb

actual fun baseEnMemoria(): PharmaDb = PharmaDb(inMemoryDriver(PharmaDb.Schema))
