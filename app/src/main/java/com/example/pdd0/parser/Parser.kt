package com.example.pdd0.parser

import android.content.Context
import android.util.Log
import com.example.pdd0.dataClass.Question
import com.google.gson.Gson

fun parseJson(context: Context): List<Question> {
    Log.d("ParseJson", "Начало парсинга JSON")  // Логирование начала парсинга

    val jsonContent = context.assets.open("pdd_res/questions/A_B/tickets/Билет 2.json").bufferedReader().use { it.readText() }
    val gson = Gson()

    // Преобразуем JSON строку в List<Question>
    val questionList = gson.fromJson(jsonContent, Array<Question>::class.java).toList()

    Log.d("ParseJson", "Парсено ${questionList.size} вопросов")


    // Логируем первые несколько вопросов, чтобы увидеть, что парсинг работает
    for (question in questionList) {
        Log.d("ParsedQuestion", "Title: ${question.title}, Question: ${question.question}")
        for (answer in question.answers) {
            Log.d("ParsedAnswer", "Answer: ${answer.answer_text}, Correct: ${answer.is_correct}")
        }
    }


    return questionList
}

