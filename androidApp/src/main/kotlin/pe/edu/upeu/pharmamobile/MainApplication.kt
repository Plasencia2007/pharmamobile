package pe.edu.upeu.pharmamobile

import android.app.Application
import org.koin.android.ext.koin.androidContext
import pe.edu.upeu.pharmamobile.di.initKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@MainApplication)
        }
    }
}
