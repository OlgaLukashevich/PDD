package com.example.pdd0

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import com.example.pdd0.dataClass.Question
import com.example.pdd0.dataClass.QuestionState
import com.example.pdd0.dataStore.FavoriteTicketsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random

class QuestionViewModel(private val favoriteTicketsManager: FavoriteTicketsManager) : ViewModel() {
    var currentQuestionIndex by mutableStateOf(0)
    var isTestFinished by mutableStateOf(false)
    var questionStates = mutableStateMapOf<Int, QuestionState>()
    var correctAnswersCount by mutableStateOf(0) // ✅ Счётчик правильных ответов
    var currentTicketStartIndex by mutableStateOf(0) // ✅ Сохраняем первый вопрос текущего билета
    // ✅ Добавляем отдельный счетчик ошибок для экзамена
    var examWrongAnswersCount by mutableStateOf(0)
        private set
    private val _ticketProgress = mutableStateMapOf<String, Float>()
    val ticketProgress: Map<String, Float> get() = _ticketProgress



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


    fun loadSpecificTicket(ticketIndex: Int) {
        questionStates.clear()
        correctAnswersCount = 0
        isTestFinished = false

        lastTicketStartIndex = ticketIndex // ✅ Запоминаем первый вопрос билета
        currentQuestionIndex = ticketIndex
    }


    fun getCurrentTicketNumber(questionList: List<Question>): String {
        val currentQuestion = questionList.getOrNull(currentQuestionIndex)
        return currentQuestion?.ticket_number ?: "Билет ?"
    }




    // Используем FavoriteTicketsManager для управления избранными билетами
    private val _favoriteTickets = MutableStateFlow<Set<String>>(emptySet())
    val favoriteTickets: StateFlow<Set<String>> get() = _favoriteTickets

    init {
        // Загружаем избранные билеты при инициализации ViewModel
        viewModelScope.launch {
            favoriteTicketsManager.favoriteTickets.collect { favorites ->
                _favoriteTickets.value = favorites
            }
        }
    }

    fun toggleFavoriteTicket(ticketNumber: String) {
        viewModelScope.launch {
            val isCurrentlyFavorite = _favoriteTickets.value.contains(ticketNumber)

            if (isCurrentlyFavorite) {
                favoriteTicketsManager.removeFavoriteTicket(ticketNumber)
            } else {
                favoriteTicketsManager.addFavoriteTicket(ticketNumber)
            }

            // ✅ Принудительно обновляем `favoriteTickets`, чтобы UI обновился
            _favoriteTickets.value = _favoriteTickets.value.toMutableSet().apply {
                if (isCurrentlyFavorite) remove(ticketNumber) else add(ticketNumber)
            }

            // Обновляем состояние для UI
            isTicketFavorite = !isCurrentlyFavorite
        }
    }


    // ✅ Функция для увеличения количества ошибок в режиме экзамена
    fun incrementExamWrongAnswers() {
        examWrongAnswersCount++
    }








}
