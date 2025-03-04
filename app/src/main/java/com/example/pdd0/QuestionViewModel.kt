package com.example.pdd0

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.*
import kotlin.random.Random

class QuestionViewModel : ViewModel() {
    var currentQuestionIndex by mutableStateOf(0)
    var isTestFinished by mutableStateOf(false)
    var questionStates = mutableStateMapOf<Int, QuestionState>()
    var correctAnswersCount by mutableStateOf(0) // ✅ Счётчик правильных ответов
    var currentTicketStartIndex by mutableStateOf(0) // ✅ Сохраняем первый вопрос текущего билета


   // var showFavoriteMessage by mutableStateOf(false)
    var isTicketFavorite by mutableStateOf(false)

    fun getCurrentQuestionState(): QuestionState {
        return questionStates[currentQuestionIndex] ?: QuestionState(null, false, false)
    }
    fun saveAnswer(answerText: String, isCorrect: Boolean) {
        val currentState = questionStates[currentQuestionIndex] ?: QuestionState(null, false, false)
        questionStates[currentQuestionIndex] = currentState.copy(
            selectedAnswer = answerText,
            isAnswerCorrect = isCorrect,
            isAnswerLocked = true
        )

        // ✅ Обновляем количество правильных ответов
        correctAnswersCount = questionStates.values.count { it.isAnswerCorrect }
        checkTestCompletion() // 🔥 Проверяем, завершён ли тест после ответа
    }

    fun loadQuestionState() {
        // Состояние уже в `questionStates`, поэтому ничего не делаем
    }

    fun saveCurrentQuestionState() {
        // Просто сохраняем текущий вопрос, не изменяя его
        if (!questionStates.containsKey(currentQuestionIndex)) {
            questionStates[currentQuestionIndex] = QuestionState(null, false, false)
        }
    }

    // Находим первый неотвеченный вопрос
    fun findFirstUnanswered(): Int? {
        return (0 until 10).firstOrNull { questionStates[it]?.selectedAnswer == null }
    }



    var lastTicketStartIndex by mutableStateOf(0) // ✅ Запоминаем номер последнего билета




    // 🔥 Новый метод для сброса теста
    fun resetTest() {
        if (currentTicketStartIndex == 0) {
            currentTicketStartIndex = (currentQuestionIndex / 10) * 10 // ✅ Запоминаем текущий билет перед сбросом
        }

        questionStates.clear() // ✅ Очищаем все ответы
        correctAnswersCount = 0 // ✅ Сбрасываем количество правильных ответов
        isTestFinished = false // ✅ Сбрасываем флаг завершения теста
        currentQuestionIndex = currentTicketStartIndex // ✅ Перенаправляем на первый вопрос текущего билета
    }


    fun loadRandomTicket() {
        questionStates.clear()
        val randomTicket = (0 until 40).random() * 10 // ✅ Выбираем случайный билет (0-39) * 10
        currentTicketStartIndex = randomTicket // ✅ Запоминаем первый вопрос билета
        currentQuestionIndex = randomTicket
        isTestFinished = false
    }






    // 🔥 Метод для перехода к следующему вопросу в рамках текущего билета
    fun moveToNextQuestion() {
        val ticketStartIndex = (currentQuestionIndex / 10) * 10 // Первый вопрос текущего билета
        if (currentQuestionIndex < ticketStartIndex + 9) { // Оставаться в рамках билета (от 0 до 9)
            currentQuestionIndex++
        }
    }





    // ✅ Проверяем, все ли вопросы в текущем билете отвечены
    fun allQuestionsAnswered(): Boolean {
        val ticketStartIndex = (currentQuestionIndex / 10) * 10 // Первый вопрос текущего билета
        return (ticketStartIndex until ticketStartIndex + 10).all { questionStates[it]?.selectedAnswer != null }
    }

    // ✅ Если все вопросы отвечены, завершаем тест
    private fun checkTestCompletion() {
        if (allQuestionsAnswered()) {
            isTestFinished = true
        }
    }


//    fun loadSpecificTicket(ticketIndex: Int) {
//        questionStates.clear() // ✅ Очищаем предыдущие ответы
//        currentTicketStartIndex = ticketIndex // ✅ Сохраняем первый вопрос этого билета
//        currentQuestionIndex = ticketIndex
//        isTestFinished = false
//    }


    fun loadSpecificTicket(ticketIndex: Int) {
        questionStates.clear()
        correctAnswersCount = 0
        isTestFinished = false

        lastTicketStartIndex = ticketIndex // ✅ Запоминаем первый вопрос билета
        currentQuestionIndex = ticketIndex
    }



    // 📌 Список избранных билетов
    var favoriteTickets = mutableStateListOf<Int>()

    // ✅ Метод для добавления/удаления билета в избранное
    fun toggleFavoriteTicket(ticketIndex: Int): Boolean {
        return if (favoriteTickets.contains(ticketIndex)) {
            favoriteTickets.remove(ticketIndex)
            isTicketFavorite = false
            false // Билет удален из избранного
        } else {
            favoriteTickets.add(ticketIndex)
            isTicketFavorite = true
            true // Билет добавлен в избранное
        }
    }

    // ✅ Проверка, является ли билет избранным
    fun isTicketFavorite(ticketIndex: Int): Boolean {
        return favoriteTickets.contains(ticketIndex)
    }

}
