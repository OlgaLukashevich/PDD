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
    val id: String
)

data class Answer(
    val answer_text: String,
    val is_correct: Boolean
)
