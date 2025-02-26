package com.example.pdd0.dataClass

// Модель для разметки
data class Markup(
    val number: String,
    val image: String,
    val description: String
)

// Структура для категорий разметки
data class MarkupCategory(
    val horizontalMarkup: Map<String, Markup>,
    val verticalMarkup: Map<String, Markup>
)

// Модель для данных разметки
data class MarkupData(
    val markup: MarkupCategory
)
