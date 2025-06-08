package com.example.pdd0.encryptData

import android.util.Base64
import javax.crypto.KeyGenerator

fun generateKey(): String {
    val keyGen = KeyGenerator.getInstance("AES")
    keyGen.init(256)  // 256-битное ключевое шифрование
    val secretKey = keyGen.generateKey()
    return Base64.encodeToString(secretKey.encoded, Base64.DEFAULT)
}
