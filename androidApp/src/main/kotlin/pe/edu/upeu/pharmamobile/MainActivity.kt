package pe.edu.upeu.pharmamobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.koin.android.ext.android.inject
import pe.edu.upeu.pharmamobile.permiso.GestorPermisosAndroid
import pe.edu.upeu.pharmamobile.permiso.PuentePermisos

class MainActivity : ComponentActivity() {

    private val gestorPermisos: GestorPermisosAndroid by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        gestorPermisos.puente = PuentePermisos(this)

        setContent {
            App()
        }
    }

    override fun onDestroy() {
        gestorPermisos.puente?.liberar()
        gestorPermisos.puente = null
        super.onDestroy()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}