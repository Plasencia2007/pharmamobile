package pe.edu.upeu.pharmamobile.navigation

sealed class Screen(val titulo: String) {
    data object Inicio : Screen("Inicio")
    data object Productos : Screen("Productos")
    data object Clientes : Screen("Clientes")
    data object Pedidos : Screen("Pedidos")
    data object Config : Screen("Ajustes")
}
