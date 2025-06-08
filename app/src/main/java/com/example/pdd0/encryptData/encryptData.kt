package com.example.pdd0.encryptData

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

fun encryptData(data: String, key: String): String {
    val cipher = Cipher.getInstance("AES")
    val secretKey: SecretKey = SecretKeySpec(key.toByteArray(), "AES")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)

    val encryptedData = cipher.doFinal(data.toByteArray())
    return Base64.encodeToString(encryptedData, Base64.DEFAULT)
}

fun decryptData(encryptedData: String, key: String): String {
    val cipher = Cipher.getInstance("AES")
    val secretKey: SecretKey = SecretKeySpec(key.toByteArray(), "AES")
    cipher.init(Cipher.DECRYPT_MODE, secretKey)

    val decodedData = Base64.decode(encryptedData, Base64.DEFAULT)
    val decryptedData = cipher.doFinal(decodedData)
    return String(decryptedData)
}
