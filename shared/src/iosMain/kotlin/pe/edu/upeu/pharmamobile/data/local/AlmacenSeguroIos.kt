@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package pe.edu.upeu.pharmamobile.data.local

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/**
 * Equivalente iOS de EncryptedSharedPreferences: el token vive en el
 * Keychain del sistema como "generic password", cifrado por iOS.
 * NO PROBADO EN EJECUCIÓN: se compila el módulo, pero no hay Mac para correrlo.
 */
class AlmacenSeguroIos : AlmacenSeguro {

    override fun guardarToken(token: String) {
        borrarToken()
        val datos = (token as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return
        val consulta = consultaBase()
        val datosRef = CFBridgingRetain(datos)
        CFDictionaryAddValue(consulta, kSecValueData, datosRef)
        SecItemAdd(consulta, null)
        CFRelease(datosRef)
        CFRelease(consulta)
    }

    override fun leerToken(): String? = memScoped {
        val consulta = consultaBase()
        CFDictionaryAddValue(consulta, kSecReturnData, kCFBooleanTrue)
        CFDictionaryAddValue(consulta, kSecMatchLimit, kSecMatchLimitOne)
        val resultado = alloc<CFTypeRefVar>()
        val estado = SecItemCopyMatching(consulta, resultado.ptr)
        CFRelease(consulta)
        if (estado != errSecSuccess) return null
        val datos = CFBridgingRelease(resultado.value) as? NSData ?: return null
        NSString.create(data = datos, encoding = NSUTF8StringEncoding)?.toString()
    }

    override fun borrarToken() {
        val consulta = consultaBase()
        SecItemDelete(consulta)
        CFRelease(consulta)
    }

    private fun consultaBase(): CFMutableDictionaryRef {
        val consulta = CFDictionaryCreateMutable(
            null, 0, kCFTypeDictionaryKeyCallBacks.ptr, kCFTypeDictionaryValueCallBacks.ptr
        )!!
        CFDictionaryAddValue(consulta, kSecClass, kSecClassGenericPassword)
        val servicio = CFBridgingRetain("pe.edu.upeu.pharmamobile.sesion")
        val cuenta = CFBridgingRetain("jwt")
        CFDictionaryAddValue(consulta, kSecAttrService, servicio)
        CFDictionaryAddValue(consulta, kSecAttrAccount, cuenta)
        CFRelease(servicio)
        CFRelease(cuenta)
        return consulta
    }
}
