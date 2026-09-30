package pe.edu.upeu.pharmamobile.data.remote

// El simulador de iOS comparte la red del Mac, así que localhost sí
// apunta a "mvnw spring-boot:run" corriendo en la máquina host.
actual val urlBaseApi: String = "http://localhost:8082/api/v1/"
