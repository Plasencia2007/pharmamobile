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

### Consumo de API (Unidad 2, Sesión 1 — Cliente Ktor)

El inventario de productos (`ProductoScreen`) se conecta al backend
[PharmaSoft](https://github.com/dreyna/pharmaSoft) mediante un `HttpClient`
de Ktor 3.6.0 configurado en `data/remote/HttpClientFactory.kt` y registrado
como `single` en Koin.

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

**Endpoint consumido**: `GET /api/v1/productos?pagina=0&tamanio=20`
(paginado, wrapper `PaginaResponseDTO`). Campos usados del
`ProductoResponseDTO` (mapeados en `data/remote/dto/ProductoDto.kt` /
`data/mapper/ProductoMapper.kt`):

- `id: Long`
- `nombre: String`
- `precio: Double`
- `stock: Int`
- `estado: Boolean` → `Producto.activo`
- `categoriaId: Long?`, `categoriaNombre: String?` (no usados aún en la UI)

El backend expone un CRUD completo (productos, categorías, clientes,
ventas, reportes), pero esta sesión solo implementa el **GET** de
productos, que es el alcance de la guía de la Sesión 1. El registro desde
el formulario de la app (`registrar()` en `ProductoRepositoryRemoto`)
todavía guarda en memoria local — conectar el `POST /api/v1/productos`
real queda para una sesión posterior.

Tráfico HTTP sin cifrar hacia `10.0.2.2` está habilitado solo para
desarrollo vía `androidApp/src/main/res/xml/network_security_config.xml`.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…