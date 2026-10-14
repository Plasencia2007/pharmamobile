package pe.edu.upeu.pharmamobile.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform
import org.koin.dsl.module
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguroIos
import pe.edu.upeu.pharmamobile.domain.platform.Compartidor
import pe.edu.upeu.pharmamobile.platform.CompartidorIos

@OptIn(ExperimentalNativeApi::class)
actual val platformModule: Module = module {
    single<HttpClientEngine> { Darwin.create() }
    single<Compartidor> { CompartidorIos() }
    single(named(NOMBRE_DEPURACION)) { Platform.isDebugBinary }
    single<AlmacenSeguro> { AlmacenSeguroIos() }
}
