package com.example.pdd0.encryptData

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

fun encryptImage(file: File, key: String): ByteArray {
    val cipher = Cipher.getInstance("AES")
    val secretKey: SecretKey = SecretKeySpec(key.toByteArray(), "AES")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)

    val inputStream = FileInputStream(file)
    val fileBytes = inputStream.readBytes()

    return cipher.doFinal(fileBytes)
}

fun decryptImage(encryptedImage: ByteArray, key: String, outputFile: File) {
    val cipher = Cipher.getInstance("AES")
    val secretKey: SecretKey = SecretKeySpec(key.toByteArray(), "AES")
    cipher.init(Cipher.DECRYPT_MODE, secretKey)

    val decryptedData = cipher.doFinal(encryptedImage)
    val outputStream = FileOutputStream(outputFile)
    outputStream.write(decryptedData)
    outputStream.close()
}
