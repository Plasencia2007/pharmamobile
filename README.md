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

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…