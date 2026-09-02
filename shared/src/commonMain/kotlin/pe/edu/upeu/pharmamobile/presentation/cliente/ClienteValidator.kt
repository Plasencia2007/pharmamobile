package pe.edu.upeu.pharmamobile.presentation.cliente

private val EMAIL_REGEX = Regex(pattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
private val TELEFONO_REGEX = Regex(pattern = "^[0-9]{6,9}$")

object ClienteValidator {

    fun validarNombre(nombre: String): String? {
        return if (nombre.isBlank()) "El nombre es obligatorio" else null
    }

    fun validarCorreo(correo: String): String? {
        return when {
            correo.isBlank() -> "El correo es obligatorio"
            !EMAIL_REGEX.matches(input = correo) -> "El correo no tiene un formato válido"
            else -> null
        }
    }

    fun validarTelefono(telefono: String): String? {
        return if (telefono.isNotBlank() && !TELEFONO_REGEX.matches(input = telefono)) {
            "El teléfono debe tener entre 6 y 9 dígitos"
        } else {
            null
        }
    }
}
