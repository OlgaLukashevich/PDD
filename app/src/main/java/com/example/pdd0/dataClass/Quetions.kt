package com.example.pdd0.dataClass

data class Question(
    val title: String,
    val ticket_number: String,
    val ticket_category: String,
    val image: String,
    val question: String,
    val answers: List<Answer>,
    val correct_answer: String,
    val answer_tip: String,
    val topic: List<String>,
    val id: String,
    val trafficSign: TrafficSign?, // Добавляем поле для знака
    val markup: Markup?           // Добавляем поле для разметки
)


data class Answer(
    val answer_text: String,
    val is_correct: Boolean
)


data class QuestionState(
    val selectedAnswer: String?,
    val isAnswerCorrect: Boolean,
    val isAnswerLocked: Boolean
)