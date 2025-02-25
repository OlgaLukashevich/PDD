package com.example.pdd0.parser

import android.content.Context
import android.util.Log
import com.example.pdd0.dataClass.Question
import com.google.gson.Gson
import java.io.FileNotFoundException

fun parseJson(context: Context): List<Question> {
    val questionList = mutableListOf<Question>()
    val gson = Gson()

    // Папка с билетами внутри assets
    val folderPath = "pdd_res/questions/A_B/tickets"

    try {
        // Получаем список всех файлов в папке
        val files = context.assets.list(folderPath)

        // Проверяем, что файлы были найдены
        if (files != null) {
            val totalFiles = files.size
//            Log.d("ParseJson", "Найдено файлов в папке: $totalFiles")

            // Если файлов меньше 40, это может быть проблемой
            if (totalFiles != 40) {
                Log.w("ParseJson", "Ожидалось 40 файлов, но найдено $totalFiles.")
            }

            // Обрабатываем каждый файл
            for (fileName in files) {
                val filePath = "$folderPath/$fileName"
                try {
                    val jsonContent = context.assets.open(filePath).bufferedReader().use { it.readText() }

                    // Преобразуем JSON строку в List<Question>
                    val questionsFromFile = gson.fromJson(jsonContent, Array<Question>::class.java).toList()

                    // Добавляем вопросы в общий список
                    questionList.addAll(questionsFromFile)

                    // Логируем, сколько вопросов из каждого файла
//                    Log.d("ParseJson", "Файл: $fileName - найдено вопросов: ${questionsFromFile.size}")
                } catch (e: Exception) {
                    Log.e("ParseJson", "Ошибка при обработке файла: $fileName", e)
                }
            }
        } else {
            Log.e("ParseJson", "Файлы в папке не найдены.")
        }
    } catch (e: FileNotFoundException) {
        Log.e("ParseJson", "Папка не найдена.", e)
    } catch (e: Exception) {
        Log.e("ParseJson", "Ошибка при парсинге JSON", e)
    }

//    // Логируем общее количество вопросов
//    Log.d("ParseJson", "Общее количество вопросов: ${questionList.size}")

    return questionList
}
