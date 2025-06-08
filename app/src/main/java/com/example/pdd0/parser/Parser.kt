package com.example.pdd0.parser

import android.content.Context
import android.util.Log
import com.example.pdd0.dataClass.*
import com.google.gson.Gson
import java.io.FileNotFoundException

fun parseJson(context: Context): List<Question> {
    val questionList = mutableListOf<Question>()
    val gson = Gson()

    // Папка с вопросами и изображениями
    val folderPathQuestion = "pdd_res/questions/A_B/tickets"
    val folderPathImg = "pdd_res/images/A_B"
    val folderPathMarkup = "pdd_res/markup"
    val folderPathSign = "pdd_res/signs"


    try {
        // Получаем список всех файлов с вопросами
        val questionFiles = context.assets.list(folderPathQuestion)
        if (questionFiles != null) {
            for (fileName in questionFiles) {
                val filePath = "$folderPathQuestion/$fileName"
                try {
                    val jsonContent = context.assets.open(filePath).bufferedReader().use { it.readText() }

                    // Преобразуем JSON строку в List<Question>
                    val questionsFromFile = gson.fromJson(jsonContent, Array<Question>::class.java).toList()

                    // Обрабатываем каждый вопрос
                    for (question in questionsFromFile) {
                        val imagePath = question.image.replace("./images/", "").replace("A_B/", "") // Убираем `./images/` и дублирующийся `A_B/`
                    // Если изображение - "no_image.jpg", заменяем на пустую строку
                        val image = if (imagePath == "no_image.jpg") " " else "$folderPathImg/$imagePath"

                        // Создаём новый объект с обновлённым путём
                        val modifiedQuestion = question.copy(image = image)

                        questionList.add(modifiedQuestion)

                    }


                } catch (e: Exception) {
                    Log.e("ParseJson", "Ошибка при обработке файла вопросов: $fileName", e)
                }
            }
        }

    } catch (e: FileNotFoundException) {
        Log.e("ParseJson", "Ошибка при поиске файлов.", e)
    } catch (e: Exception) {
        Log.e("ParseJson", "Ошибка при парсинге JSON", e)
    }

    return questionList
}
