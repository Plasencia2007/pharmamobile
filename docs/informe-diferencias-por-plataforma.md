# Informe comparativo de diferencias por plataforma

Actividad Autónoma N.º 09 · PharmaMobile. Todo lo "observado" salió de ejecutar el proyecto en el emulador Android o del compilador. Lo que no pude ejecutar (iOS, por falta de macOS) está marcado como **no observado**.

## 1. ¿Por qué la API de formato de moneda difiere si ambas usan el mismo locale?

El locale es el mismo, pero lo que cambia es quién implementa el estándar. En Android, `formatearSoles` (`platform/Formato.android.kt`) llama a `NumberFormat.getCurrencyInstance(Locale("es", "PE"))`, que usa los datos de localización (ICU/CLDR) que trae la imagen del sistema. En iOS (`Formato.ios.kt`) uso `NSNumberFormatter`, que lee los datos de Foundation, empaquetados por Apple y con su propia versión del estándar. Son dos implementaciones del mismo estándar y no tienen por qué coincidir carácter por carácter.

Resultado literal en Android para el precio 12.0: `S/` + U+00A0 + `12.00`, es decir, un espacio de no separación entre el símbolo y la cifra, con punto decimal. Lo comprobé leyendo los puntos de código del texto del selector de compartir: `0x53 0x2f 0xa0 0x31 0x32 0x2e`. Antes de usar `formatearSoles` la app mostraba `S/ 12.0`, porque interpolaba el `Double` a mano.

En iOS el resultado literal es **no observado**. La guía sugiere que podría salir `S/12.00`, sin espacio, pero eso es una hipótesis suya y no un dato mío. Sí hay una diferencia verificable en el código: `stringFromNumber` devuelve un valor anulable, por eso el `actual` de iOS necesita el respaldo `?: "S/ $valor"`. Android devuelve siempre un `String`.

## 2. ¿Por qué `CompartidorAndroid` necesita un `Context` y `CompartidorIos` no?

En Android, abrir otra pantalla es una operación de un componente: `startActivity` es un método de `Context`, y el selector del sistema es una Activity que se lanza mediante un `Intent`. Por eso `CompartidorAndroid(contexto)` lo recibe por constructor. Koin lo entrega con `androidContext()`, que devuelve el `Context` de la aplicación que `MainApplication` registró en `initKoin { androidContext(this@MainApplication) }`. Ese contexto no es el de una Activity, y lo comprobé en el sistema: el chooser arrancó con `flg=0x10000000`, que es `FLAG_ACTIVITY_NEW_TASK`, la bandera que obliga a poner un contexto de aplicación.

UIKit funciona distinto: expone un objeto global, `UIApplication.sharedApplication`, y para mostrar la hoja basta presentar un `UIActivityViewController` desde el `rootViewController` de la ventana activa. Ese controlador se obtiene en el momento de compartir, así que no hay nada que inyectar y `CompartidorIos()` no recibe parámetros. Este camino es **no observado** en ejecución. Mi código no hace nada si `rootViewController` es nulo, y `keyWindow` está marcado como obsoleto en las versiones recientes de iOS.

## 3. ¿Qué habría cambiado con una interfaz e inyección en lugar de `expect/actual`?

Usé las dos estrategias a propósito. `formatearSoles` es `expect/actual` porque es una función pura sin dependencias. `Compartidor` es una interfaz en `domain` con su implementación registrada en cada `platformModule`.

Si `Compartidor` hubiera sido un `expect class`, la firma del constructor tendría que ser idéntica en las dos plataformas, y no podría pedir un `Context` solo en Android. Habría tenido que esconder el contexto en una variable global, que es un acoplamiento oculto. Además no habría podido sustituirlo en pruebas: con la interfaz escribí `CompartidorFalso` y las 3 pruebas de `DetalleProductoViewModelTest` comprueban qué texto recibe, sin tocar Android.

A la inversa, si `formatearSoles` fuera una interfaz, `Producto.toUi()` (una función de extensión sin acceso a Koin) no podría recibirla, y olvidar registrarla fallaría en tiempo de ejecución en vez de al compilar. Con `expect/actual` el error aparece al compilar, como se ve en la pregunta 4. La interfaz da más flexibilidad para probar y el `expect` da más seguridad en tiempo de compilación; elegí cada una según si la capacidad tiene dependencias.

## 4. ¿Qué ocurre si falta un `actual`?

El proyecto deja de compilar solo en la plataforma afectada. Comenté uno a la vez y compilé ambos targets. Sin el `actual` de iOS (`Formato.ios.kt`):

```
e: shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobile/platform/Formato.kt:3:1 Expected formatearSoles has no actual declaration in module <commonMain> for Native
> Task :shared:compileKotlinIosSimulatorArm64 FAILED
```

Sin el `actual` de Android (`Formato.android.kt`):

```
e: shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobile/platform/Formato.kt:3:1 Expected formatearSoles has no actual declaration in module <commonMain> for JVM
> Task :shared:compileAndroidMain FAILED
```

En el primer caso Android siguió compilando; en el segundo, iOS. El mensaje distingue el destino (`Native` o `JVM`) y señala el `expect` de `commonMain`. Después restauré ambos archivos y el proyecto volvió a compilar.

## 5. ¿Qué capacidad NO debería bajar al código de plataforma?

Las reglas de negocio de `Producto`. `estaInactivo()` y `requiereReposicion()` (con `STOCK_MINIMO = 5`) deciden en qué pestaña aparece cada producto y qué color de estado muestra. Son cálculo puro sobre `stock` y `activo`, con el mismo resultado en las dos plataformas. Si bajaran a `androidMain` e `iosMain` habría dos copias que podrían divergir, y un producto sería "bajo stock" en un teléfono y no en el otro.

El criterio que uso: un código baja a la plataforma solo si depende de una API o de datos del sistema operativo (formatos locales, `Intent`, `UIKit`, versión del sistema). Si el resultado es determinista y no toca el sistema, se queda en `commonMain`. El cliente HTTP es otro ejemplo: `HttpClientFactory` es común y solo el motor (OkHttp en Android, Darwin en iOS) vive en `PlatformModule`. `ejecutarLlamada` usa `kotlinx.io.IOException` en vez de `java.io.IOException` por la misma razón.

## Nota sobre la tercera capacidad

`InfoDispositivo` usa `Build.VERSION.RELEASE` (Android) y `UIDevice` (iOS). La plantilla de KMP ya traía `getPlatform()`, que devuelve `Android 35` (nivel de API, `SDK_INT`) y no la versión legible. En el emulador, `InfoDispositivo` muestra `Android` y `15`. El compilador avisa que `expect class` está en Beta; dejé el aviso a la vista.
