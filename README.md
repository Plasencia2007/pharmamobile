This is a Kotlin Multiplatform project targeting Android, iOS.

* [/iosApp](./iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

### Conectividad REST (Unidad 2 — Cliente Ktor + CRUD)

El inventario de productos (`ProductoScreen`) se conecta al backend
[PharmaSoft](https://github.com/dreyna/pharmaSoft) mediante un `HttpClient`
de Ktor 3.6.0 configurado en `data/remote/HttpClientFactory.kt` y registrado
como `single` en Koin.

**URL base**: `http://10.0.2.2:8082/api/v1/` (Android, emulador) /
`http://localhost:8082/api/v1/` (iOS, simulador) — versión de API: `v1`.

**Catálogo de endpoints** (recurso `productos`, implementado en
`data/remote/ProductoApi.kt`):

| Método | Ruta | Parámetros | Respuesta | Errores |
|---|---|---|---|---|
| GET | `/productos` | `pagina`, `tamanio` (query) | 200 · `PaginaResponseDto<ProductoResponseDto>` | 500 |
| GET | `/productos/{id}` | `id` (ruta) | 200 · `ProductoResponseDto` | 400, 404 |
| POST | `/productos` | cuerpo `ProductoRequestDto` | 201 · `ProductoResponseDto` | 400, 409 |
| PUT | `/productos/{id}` | `id` + cuerpo `ProductoRequestDto` | 200 · `ProductoResponseDto` | 400, 404, 409 |
| DELETE | `/productos/{id}` | `id` (ruta) | 204 · sin cuerpo | 404, 409 |

`DELETE` es un borrado lógico (`estado = false`): repetirlo sobre un
producto ya inactivo responde 409, no 204.

### Capacidades nativas (expect/actual)

Dos capacidades que dependen del sistema operativo, resueltas con dos
mecanismos distintos a propósito:

| Capacidad | Mecanismo | Por qué |
|---|---|---|
| Formato de moneda (`formatearSoles`) | `expect`/`actual` | Función pura sin dependencias: el compilador exige el `actual` de cada plataforma |
| Compartir (`Compartidor`) | Interfaz en `domain` + implementación inyectada por Koin | Necesita un objeto de plataforma (`Context` / controlador de vista) y se puede sustituir en pruebas |

| Archivo | Source set | Qué hace |
|---|---|---|
| `platform/Formato.kt` | `commonMain` | `expect fun formatearSoles(valor: Double): String` |
| `platform/Formato.android.kt` | `androidMain` | `NumberFormat` con `Locale("es", "PE")` |
| `platform/Formato.ios.kt` | `iosMain` | `NSNumberFormatter` con `NSLocale("es_PE")` |
| `domain/platform/Compartidor.kt` | `commonMain` | Interfaz `Compartidor.compartir(texto)` |
| `platform/CompartidorAndroid.kt` | `androidMain` | `Intent.ACTION_SEND` + `createChooser` (`FLAG_ACTIVITY_NEW_TASK`) |
| `platform/CompartidorIos.kt` | `iosMain` | `UIActivityViewController` desde el `rootViewController` activo |
| `di/PlatformModule.android.kt` / `.ios.kt` | por plataforma | Registran `HttpClientEngine` y `Compartidor` |

El texto a compartir se arma en código común
(`domain/usecase/TextoParaCompartir.kt`) y el formato de moneda se aplica
en la capa de presentación (`ProductoUi`, `Producto.toUi()`), nunca en el
dominio ni dentro del composable. `presentation` y `domain` no importan
`android.` ni `platform.UIKit`/`platform.Foundation`.

El `actual` de iOS **compila** (`./gradlew :shared:compileKotlinIosSimulatorArm64`)
pero no se ejecutó: el desarrollo se hizo en Windows y ejecutar el
simulador exige macOS con Xcode.

### Código específico de plataforma

Inventario de todo lo que depende del sistema operativo. Las rutas son
relativas a `shared/src/` y el paquete es `pe/edu/upeu/pharmamobile/`.

| Capacidad | Mecanismo y firma | `actual` / implementación en Android | `actual` / implementación en iOS |
|---|---|---|---|
| Formato de moneda | `expect fun formatearSoles(valor: Double): String` (`commonMain/.../platform/Formato.kt`) | `androidMain/.../platform/Formato.android.kt` · `NumberFormat` con `Locale("es", "PE")` | `iosMain/.../platform/Formato.ios.kt` · `NSNumberFormatter` con `NSLocale("es_PE")` |
| Información del dispositivo | `expect class InfoDispositivo()` con `sistema` y `version` (`commonMain/.../platform/InfoDispositivo.kt`) | `androidMain/.../platform/InfoDispositivo.android.kt` · `Build.VERSION.RELEASE` | `iosMain/.../platform/InfoDispositivo.ios.kt` · `UIDevice.currentDevice.systemName` / `systemVersion` |
| Compartir producto | **No es `expect`**: `interface Compartidor` (`commonMain/.../domain/platform/Compartidor.kt`) | `androidMain/.../platform/CompartidorAndroid.kt` · `Intent.ACTION_SEND` + `createChooser` | `iosMain/.../platform/CompartidorIos.kt` · `UIActivityViewController` |
| Módulo de inyección | `expect val platformModule: Module` (`commonMain/.../di/PlatformModule.kt`) | `androidMain/.../di/PlatformModule.android.kt` · `module` con `androidContext()`, motor OkHttp | `iosMain/.../di/PlatformModule.ios.kt` · `module` sin contexto, motor Darwin |
| URL base de la API | `expect val urlBaseApi: String` (`commonMain/.../data/remote/HttpClientFactory.kt`) | `androidMain/.../data/remote/HttpClientFactory.android.kt` · `10.0.2.2` | `iosMain/.../data/remote/HttpClientFactory.ios.kt` · `localhost` |
| Nombre de la plataforma | `expect fun getPlatform(): Platform` (`commonMain/.../Platform.kt`, de la plantilla KMP) | `androidMain/.../Platform.android.kt` · `Build.VERSION.SDK_INT` | `iosMain/.../Platform.ios.kt` · `UIDevice` |

`commonMain` no contiene ninguna importación `android.`, `platform.` (frameworks de Apple) ni `java.`.
El informe comparativo con las diferencias observadas entre plataformas está en
[`docs/informe-diferencias-por-plataforma.md`](docs/informe-diferencias-por-plataforma.md).

### Manejo de errores

`data/remote/EjecutarLlamada.kt` es el único punto que traduce las
excepciones de Ktor a `ErrorApi` (`domain/error/ErrorApi.kt`); el resto
del código (repositorio, casos de uso, `ViewModel`) solo conoce
`Result<T>` y `ErrorApiException`.

| Excepción de Ktor | `ErrorApi` | Mensaje al usuario |
|---|---|---|
| `ClientRequestException` (400) | `Validacion(porCampo)` | Mensaje real del servidor bajo el campo (`nombre`/`precio`/`stock`) |
| `ClientRequestException` (404) | `NoEncontrado` | "El producto ya no existe." |
| `ClientRequestException` (409) | `Conflicto(mensaje)` | Mensaje real del servidor (regla de negocio) |
| `ServerResponseException` (5xx) | `Servidor` | "El servidor no pudo procesar la solicitud..." |
| `HttpRequestTimeoutException` | `TiempoAgotado` | "La solicitud tardó demasiado..." |
| `IOException` | `SinConexion` | "No hay conexión con el servidor." |
| `CancellationException` | — | Se relanza sin traducir (no debe tratarse como error) |

**Hallazgos de la bitácora de pruebas** (ver
`docs/S08_ActividadAutonoma_Plasencia.pdf` para el detalle completo de
los 8 escenarios con capturas):

- **DELETE es un borrado lógico**: repetir un DELETE sobre un producto ya
  inactivo responde **409** ("ya se encuentra inactivo"), no 404 como
  asumiría un CRUD con borrado físico.
- **Regla de negocio real**: crear/actualizar un producto con un nombre
  ya existente responde 409 con `ErrorApi.Conflicto`.
- **Cancelación (hallazgo importante)**: salir de la pantalla de
  Productos mientras una operación está en curso **no cancela la
  corrutina**. `App.kt` cambia de pantalla con un simple `when` sobre un
  `remember { mutableStateOf<Screen> }`, sin un `NavHost` que le dé a
  cada pantalla su propio `ViewModelStoreOwner`; `ProductoViewModel`
  vive a nivel de Activity y sigue corriendo en segundo plano. El
  resultado (éxito o error) se aplica igual, y el usuario solo lo ve si
  vuelve a esa pantalla — no hay crash, pero tampoco hay cancelación
  real. Documentado como mejora pendiente: la pantalla necesitaría su
  propio `ViewModelStoreOwner` (por ejemplo con Navigation Compose) para
  que `viewModelScope` se cancele al salir.

**Levantar el backend localmente** (requiere PostgreSQL; el repo de
PharmaSoft usado aquí fue portado de Oracle a Postgres):

```bash
docker run -d --name pharmasoft-postgres \
  -e POSTGRES_DB=pharmasoft -e POSTGRES_USER=pharmasoft -e POSTGRES_PASSWORD=pharmasoft \
  -p 5434:5432 postgres:16-alpine

# en el checkout de PharmaSoft, con JDK 21 y el perfil "dev":
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

El backend queda en `http://localhost:8082` (puerto 8082 porque 8080/8081
ya estaban en uso en esta máquina de desarrollo; ajustar
`server.port` en `application-dev.yaml` y las constantes `urlBaseApi` de
abajo si se usa otro puerto).

**URL base por plataforma** (`data/remote/HttpClientFactory.*.kt`):

| Plataforma | URL base | Motivo |
|---|---|---|
| Android (emulador) | `http://10.0.2.2:8082/api/v1/` | `10.0.2.2` es el alias que usa el emulador para llegar al `localhost` de la máquina host |
| iOS (simulador) | `http://localhost:8082/api/v1/` | El simulador comparte la red del Mac, así que sí ve el `localhost` real |

**Diccionario de DTO** (`data/remote/dto/ProductoDto.kt`):

| Campo JSON | Tipo Kotlin | Obligatorio | Valor por defecto | Campo en el dominio |
|---|---|---|---|---|
| `id` | `Long` | Sí (solo respuesta) | — | `Producto.id` |
| `nombre` | `String` | Sí | — | `Producto.nombre` |
| `precio` | `Double` | Sí | — | `Producto.precio` |
| `stock` | `Int` | Sí | — | `Producto.stock` |
| `estado` | `Boolean` | No | `true` | `Producto.activo` |
| `categoriaId` | `Long?` | Sí (request) / No (response) | `null` (response) | no mapeado aún |
| `categoriaNombre` | `String?` | No (solo respuesta) | `null` | no mapeado aún |

El backend expone un CRUD completo (productos, categorías, clientes,
ventas, reportes). La app implementa las cinco operaciones sobre
`productos` (Sesión 8); categorías/clientes/ventas quedan para sesiones
posteriores. El registro/edición usa una `CATEGORIA_POR_DEFECTO` fija
(`di/AppModule.kt`) porque aún no hay selector real de categoría en el
formulario.

Tráfico HTTP sin cifrar hacia `10.0.2.2` está habilitado solo para
desarrollo vía `androidApp/src/main/res/xml/network_security_config.xml`.

### Autenticación (JWT)

La app inicia sesión contra `POST /api/v1/auth/login` de PharmaSoft, guarda
el token en el almacén cifrado de cada plataforma y lo adjunta a todas las
peticiones con el plugin `Auth` de Ktor: ningún repositorio escribe la
cabecera `Authorization` a mano.

**Usuarios de prueba** (sembrados por Flyway en el backend del curso):

| Usuario | Contraseña | Rol | Permisos |
|---|---|---|---|
| `admin` | `Admin123` | ADMIN | Los ocho permisos |
| `vendedor` | `Vendedor123` | VENDEDOR | Catálogo solo lectura; clientes y ventas completos |
| `almacen` | `Almacen123` | ALMACENERO | Catálogo: leer y escribir |

**Capas** (ningún archivo de `domain` importa Ktor, Android ni UIKit):

| Capa | Piezas |
|---|---|
| `domain` | `Sesion` (con `puede(permiso)`), `Credenciales`, `ErrorSesion`, `AuthRepository`, `GestorSesion` (única fuente de verdad de la sesión), `IniciarSesionUseCase`, `CerrarSesionUseCase` |
| `data` | `AuthRepositorioRest`, `LoginDto`, `AlmacenSeguro` (interfaz), `HttpClientFactory` con `Auth` + `Logging` |
| `androidMain` | `AlmacenSeguroAndroid`: `EncryptedSharedPreferences` con llave AES-256 en el Android Keystore |
| `iosMain` | `AlmacenSeguroIos`: Keychain (`SecItemAdd` / `SecItemCopyMatching`). **Solo compila; no se ejecutó (no hay Mac)** |
| `presentation` | `LoginScreen`, `SesionViewModel`; `App` decide entre login y contenido según la sesión |

**Decisiones**

- `AlmacenSeguro` es una interfaz inyectada y no un `expect class`: la
  versión de Android necesita un `Context` y la de iOS no recibe nada.
- `sendWithoutRequest` devuelve `true` salvo en `/auth/login`: el token
  viaja ya en la primera petición, sin el ciclo "401 y reintento".
- Tras guardar o borrar el token se invoca `clearToken()` del proveedor
  bearer, porque `loadTokens` queda en caché.
- `Logging` usa `sanitizeHeader` para `Authorization` y queda en
  `LogLevel.NONE` fuera de depuración (`FLAG_DEBUGGABLE` en Android,
  `Platform.isDebugBinary` en iOS). En el log de Ktor la cabecera sale como
  `Authorization: ***`.
- **401 y 403 son distintos.** Un 401 en una ruta protegida (token vencido,
  alterado o ausente) borra el token, limpia el estado y lleva al login con
  un aviso. Un 403 mantiene la sesión y muestra "No tienes permiso para
  realizar esta operación". En el login, un 401 significa credenciales
  incorrectas y no cierra nada.
- La interfaz oculta lo que el rol no puede usar (`sesion.puede(...)`): el
  botón de registrar y editar/eliminar sin `CATALOGO_ESCRIBIR`, y las
  secciones Clientes y Pedidos sin `CLIENTES_LEER` / `VENTAS_LEER`.
- Al reabrir la app, `sesionGuardada()` lee el token y consulta `/auth/me`
  para reconstruir la sesión; si el servidor responde 401 el token se
  descarta.
- El token dura 3600 s y el backend no tiene endpoint de renovación: al
  vencer hay que volver a iniciar sesión.

**Evidencias** en `docs/evidencias/s10_*` (capturas del emulador Android,
registros de Ktor con la cabecera saneada, comprobación del almacén cifrado
y matriz de permisos ejecutada contra el servidor).

### Persistencia offline-first

Desde la sesión 11 la interfaz **lee siempre de una base local** (SQLDelight) y
la red pasa a ser un proceso de segundo plano cuya única misión es mantener esa
base al día. La app abre y lista productos aunque PharmaSoft esté apagado.

| Pieza | Dónde | Qué hace |
|---|---|---|
| `Producto.sq` | `commonMain/sqldelight/.../db` | Tabla `producto` con la marca `pendiente` y las consultas; SQLDelight genera `PharmaDb` y valida el SQL al compilar |
| `CreadorDeDriver` | `commonMain/data/local` | Interfaz del driver; `CreadorDeDriverAndroid` (`AndroidSqliteDriver`) y `CreadorDeDriverIos` (`NativeSqliteDriver`, solo compila) se registran en cada `platformModule` |
| `ProductoLocal` | `commonMain/data/local` | Consultas y traducción fila ↔ dominio. No importa nada de Ktor ni de los DTO |
| `ProductoRepository` | `commonMain/domain` | `observar(): Flow`, `registrar`, `sincronizar()` (más `obtener`, `actualizar` y `eliminar`) |
| `ProductoRepositorioOffline` | `commonMain/data/repository` | Implementación: escribe en local, sube lo pendiente y después baja del servidor |
| `ProductoViewModel` | `commonMain/presentation` | Se suscribe una vez a `observar()` y sincroniza en segundo plano; ya no recarga la lista a mano |

**Reglas que se cumplen**

- **Primero subir, después bajar.** `sincronizar()` envía lo pendiente y luego
  trae la lista del servidor. Al revés, la bajada borraría lo creado sin red.
- **Ids locales negativos.** Un producto creado sin red recibe un id negativo
  (`-1`, `-2`…): un id 0 podría chocar con uno del servidor.
- **`borrarSincronizados` solo borra `pendiente = 0`** y todo el reemplazo va en
  una transacción: sin ella, el `Flow` emite una vez por fila y la lista parpadea.
- **Un producto que el servidor ya aceptó se borra de la tabla local** antes de
  traer la lista. La guía no lo hace y, tal como está escrita, el pendiente
  quedaría marcado y volvería a enviarse en cada sincronización (duplicados);
  hay una prueba que lo cubre.
- Un fallo de red no tumba la operación local: el producto queda pendiente
  («Pendiente de enviar») y la pantalla muestra «Sin conexión: mostrando datos
  guardados».

**Límites declarados.** Crear y editar un producto *pendiente* funciona sin red;
editar o eliminar un producto *ya sincronizado* sí necesita conexión (queda fuera
del alcance de la guía). Si el servidor rechaza un pendiente, la sincronización
se detiene y se avisa. El driver de iOS solo se compiló: no hay Mac.

**Pruebas** (`./gradlew :shared:testAndroidHostTest`): las pruebas offline viven en
`commonTest` y usan una base SQLite en memoria nueva por prueba (JDBC en la JVM,
driver nativo en iOS) y un servidor simulado con `ktor-client-mock`; cubren el
camino con red, el camino sin red, el orden subir-bajar y la ausencia de duplicados.

**Versión de SQLDelight.** La guía indica 2.1.0, pero con AGP 9 (plugin
`com.android.kotlin.multiplatform.library`) esa versión rompe el script de Gradle
(el bloque `android {}` deja de resolverse); se usa 2.4.1.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…