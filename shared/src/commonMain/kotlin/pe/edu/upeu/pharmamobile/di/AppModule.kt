package pe.edu.upeu.pharmamobile.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobile.data.repository.AuthRepositorioRest
import pe.edu.upeu.pharmamobile.data.repository.ProductoRepositorioRest
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobile.domain.session.GestorSesion
import pe.edu.upeu.pharmamobile.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ProbarAccesoUseCase
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ObtenerProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobile.presentation.detalle.DetalleProductoViewModel
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoViewModel

/**
 * El backend exige categoriaId en cada POST/PUT y esta app todavia no
 * tiene una pantalla de categorias (fuera del alcance de la Sesion 8).
 * 1L corresponde a la categoria "Analgesicos" sembrada en PharmaSoft para
 * las pruebas de esta practica.
 */
private const val CATEGORIA_POR_DEFECTO = 1L

val dataModule = module {
    single {
        crearHttpClient(
            engine = get<HttpClientEngine>(),
            almacen = get(),
            esDepuracion = get(named(NOMBRE_DEPURACION)),
            // Se resuelve al usarse: GestorSesion depende del repositorio, que depende de este cliente.
            alSesionExpirada = { get<GestorSesion>().expirar() }
        )
    }
    single { ProductoApi(get<HttpClient>()) }
    single<AuthRepository> { AuthRepositorioRest(get(), get()) }
    single { GestorSesion(get()) }
    single<ProductoRepository> { ProductoRepositorioRest(get(), CATEGORIA_POR_DEFECTO) }
}

val domainModule = module {
    factory { IniciarSesionUseCase(get()) }
    factory { CerrarSesionUseCase(get()) }
    factory { ProbarAccesoUseCase(get()) }
    factory { ListarProductosUseCase(get()) }
    factory { ObtenerProductoUseCase(get()) }
    factory { RegistrarProductoUseCase(get()) }
    factory { ActualizarProductoUseCase(get()) }
    factory { EliminarProductoUseCase(get()) }
}

val presentationModule = module {
    viewModel { ProductoViewModel(get(), get(), get(), get()) }
    viewModel { (productoId: Long) -> DetalleProductoViewModel(productoId, get(), get()) }
}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(platformModule, dataModule, domainModule, presentationModule)
    }
}
