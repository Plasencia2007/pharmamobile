package pe.edu.upeu.pharmamobile

import android.app.Application
import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import pe.edu.upeu.pharmamobile.di.initKoin
import pe.edu.upeu.pharmamobile.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobile.domain.port.Notificador
import pe.edu.upeu.pharmamobile.permiso.GestorPermisosAndroid
import pe.edu.upeu.pharmamobile.permiso.NotificadorAndroid

/** Lo que necesita clases de Android que solo existen en este modulo. */
private val appAndroidModule = module {
    single { GestorPermisosAndroid(get(), get<Context>().getSharedPreferences("permisos", Context.MODE_PRIVATE)) }
    single<GestorPermisos> { get<GestorPermisosAndroid>() }
    single<Notificador> { NotificadorAndroid(get()) }
}

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@MainApplication)
            modules(appAndroidModule)
        }
    }
}
