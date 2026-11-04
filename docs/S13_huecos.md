# Sesión 13 · Tabla de huecos del recorrido completo

Recorrido hecho el 4 de noviembre de 2026 en el emulador Pixel_8 (Android 15), con la app instalada desde cero
(`pm clear`), contra PharmaSoft en ejecución y sobre la versión anterior a esta rama (commit `8e732e9`).

## Lo que se observó en cada momento

| N.º | Momento | Observado | ¿Hueco? |
|---|---|---|---|
| 1 | Primera apertura, sin sesión | Pantalla de login con explicación de la app. | No |
| 2 | Credenciales incorrectas | «Usuario o contraseña incorrectos.» Sin red: «No hay conexión con el servidor.» Los distingue. | No |
| 3 | Listado con la base vacía | Estado vacío con texto propio y «Nunca sincronizado» (actividad 11). | No |
| 4 | Creación en modo avión | Se guarda con id -1 y marca de pendiente. Pero el estado sigue diciendo «Sincronizado» porque la app no sabe que no hay red hasta intentar. | H1 |
| 5 | Sincronización al recuperar la señal | 25 s después de volver la señal, el pendiente sigue ahí: hay que pulsar el botón. No se duplica. | H2 |
| 6 | Error del servidor | Con un nombre repetido el servidor responde 409. La fila queda pendiente para siempre, el formulario dice que se registró, la app afirma «Sin conexión» y **la bajada deja de funcionar** porque la subida lanza antes. | H3, H4, H5 |
| 7 | Token vencido a mitad de un formulario | Lleva al login con aviso claro, pero se pierde lo escrito en la edición. | H6 |
| 8 | Cierre de sesión | El token se borra. La base local no: el siguiente usuario ve el inventario y hereda los pendientes del anterior. | H7 |
| — | Navegación | Al volver a Inventario se reabre solo el formulario «Nuevo producto» (defecto del rediseño). | H8 |
| — | Ajustes | «Alertas de stock bajo» no hace nada, no se guarda y dice 10 uds. cuando el mínimo real es 5. | H9 |
| — | Edición sin red | Editar un producto ya sincronizado exige red: no se encola. | H10 |

## Huecos y decisión

| Hueco | Descripción | Gravedad | Decisión |
|---|---|---|---|
| H3 | Una fila rechazada por el servidor queda pendiente para siempre, sin explicación | Alta | **Hoy** (pasos 2 y 3): intentos, último error y fila fallida visible |
| H4 | Un rechazo se informa como «Sin conexión» | Alta | **Hoy** (pasos 3 y 4) |
| H5 | Una fila rechazada bloquea la bajada de toda la lista | Alta | **Hoy** (paso 3): la subida ya no corta la sincronización |
| H8 | El formulario se reabre solo al volver a Inventario | Media | **Hoy**: la solicitud del «+» se consume una vez atendida |
| H9 | El ajuste de alertas no hace nada | Media | **Hoy** (paso 5): pasa a ser el permiso real de notificaciones |
| H1 | «Sincronizado» aunque haya cambios sin enviar | Media | **Hoy** (paso 4) el mensaje dice cuántos faltan. El estado real de la red se cierra con H2 |
| H2 | No se sincroniza sola al volver la señal | Media | **Sesión 14**: necesita observar la conectividad en cada plataforma. Hoy el botón resuelve el caso y no se pierde nada |
| H6 | Se pierde lo escrito si la sesión vence al editar | Media | **Sesión 14**: encolar las ediciones (junto con H10) permite no depender del servidor al guardar |
| H10 | Editar un producto sincronizado exige red | Media | **Sesión 14**: la cola de hoy solo cubre altas; extenderla es trabajo propio |
| H7 | El siguiente usuario hereda los datos y pendientes del anterior | Media | **Sesión 14**: borrar al cerrar sesión perdería pendientes, así que requiere decidir el aviso previo. Mientras tanto, si el usuario no tiene permiso, el servidor rechaza el envío y la fila queda visible como fallida |

| H11 | En un alta, un error de validación del servidor ya no llega al campo: el producto se guarda en local y la fila queda fallida con el motivo | Media | **Sesión 14**: validar en el teléfono las mismas reglas que el servidor (nombre de 3 a 150 caracteres). La edición sí muestra el error en su campo |

## Fechas de cierre

Los huecos H1, H3, H4, H5, H8 y H9 y los defectos de abajo se cerraron el 4 de noviembre de 2026. H2, H6, H7, H10 y H11 tienen fecha de cierre en la sesión 14, el 10 de noviembre de 2026. Ninguno se descartó.

## Defectos encontrados al probar contra el servicio real

| Defecto | Corrección |
|---|---|
| Un servidor colgado se trataba como «sin conexión» y la fila no gastaba intentos | `SocketTimeoutException` pasa a tiempo agotado (`0213a64`) |
| El aviso decía «Sin conexión» aunque había red | Aviso propio «No se pudo sincronizar» (`96985d4`) |
| Al conceder el permiso desde Ajustes tras negarlo, las alertas quedaban apagadas | Conceder el permiso las reactiva (`96985d4`) |
