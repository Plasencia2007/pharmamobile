package pe.edu.upeu.pharmamobile.domain.demo

import pe.edu.upeu.pharmamobile.domain.result.ResultadoProductos

fun mostrarResultados(resultado: ResultadoProductos) {
    when (resultado) {
        ResultadoProductos.Cargando -> {
            println(
                "Cargando productos"
            )
        }
        is ResultadoProductos.Exito -> {
            println("Productos encontrados: ${resultado.list.size}")
        }
        is ResultadoProductos.Error -> {
            println("Error: ${resultado.msg}")
        }
    }
}
