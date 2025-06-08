package com.example.pdd0

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.pdd0.dataClass.Question
import com.example.pdd0.dataClass.QuestionState
import com.example.pdd0.dataStore.FavoriteTicketsManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random

class QuestionViewModel(private val favoriteTicketsManager: FavoriteTicketsManager) : ViewModel() {
    var currentQuestionIndex by mutableStateOf(0)
    var isTestFinished by mutableStateOf(false)
    var questionStates = mutableStateMapOf<Int, QuestionState>()
    var correctAnswersCount by mutableStateOf(0) //  Счётчик правильных ответов
    var currentTicketStartIndex by mutableStateOf(0) //  Сохраняем первый вопрос текущего билета
    //  отдельный счетчик ошибок для экзамена
    var examWrongAnswersCount by mutableStateOf(0)
        private set

    var isTicketFavorite by mutableStateOf(false)


    // Новый список для хранения индексов неправильных вопросов
    var incorrectQuestions = mutableStateListOf<Int>()
    // Состояние комментариев для каждого вопроса
    var questionCommentsState = mutableStateMapOf<Int, Boolean>()
    val explanationTexts = mutableStateMapOf<Int, String>() // <--- новое хранилище


     // Новый метод для сброса состояний комментариев
    fun resetCommentStates() {
        questionCommentsState.clear() // Очистить все состояния комментариев
      //  isCommentVisible.value = true  // Сбросить видимость комментариев, если тест начинается заново
    }

    // Метод для скрытия комментария для конкретного вопроса
    fun hideCommentForQuestion(questionIndex: Int) {
        questionCommentsState[questionIndex] = false
      //  isCommentVisible.value = false // Скрыть комментарий
    }


    // Метод для получения состояния комментария
    fun getCommentStateForQuestion(questionIndex: Int): Boolean {
        return questionCommentsState[questionIndex] ?: true // по умолчанию показываем комментарий
    }

    // Список вопросов
    var questionList: List<Question> = emptyList()


    // Таймер в миллисекундах
    private val _timeLeft = MutableLiveData<Long>()
    val timeLeft: LiveData<Long> get() = _timeLeft

    var isTimeUp by mutableStateOf(false) // Для отслеживания, когда таймер истечет
    private var isTimerPaused by mutableStateOf(false) // Track the pause state of the timer
    private var timerJob: Job? = null // Coroutine job to manage the timer


    // Начальная длительность таймера (3 минуты)
    init {
        _timeLeft.value = 5 * 60 * 1000L

    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (_timeLeft.value!! > 0 && !isTimerPaused) {
                delay(1000L) // уменьшать время каждую секунду
                _timeLeft.value = _timeLeft.value!! - 1000L
            }
            if (_timeLeft.value!! <= 0) {
                isTimeUp = true
            }
        }
    }

    // Пауза таймера
    fun pauseTimer() {
        isTimerPaused = true
        timerJob?.cancel() // Останавливаем текущий корутин
    }

    // Возобновить таймер
    fun resumeTimer() {
        if (isTimerPaused) {
            isTimerPaused = false // Снимаем паузу
            startTimer() // Таймер продолжит отсчет с того места, где был приостановлен

        }
    }

    fun resetTimerToInitial() {
        // Сбрасываем таймер на начальное значение (например, 5 минут)
        _timeLeft.value = 5 * 60 * 1000L // 5 минут в миллисекундах

        pauseTimer()
        resetExamCounters()
    }


    // Метод для сброса счетчика ошибок
    fun resetExamCounters() {
        examWrongAnswersCount = 0 // Сбрасываем количество ошибок
        isTimeUp = false // Сбрасываем таймер
    }


    fun getCurrentQuestionState(): QuestionState {
        return questionStates[currentQuestionIndex] ?: QuestionState(null, false, false)
    }


    fun saveAnswer(answerText: String, isCorrect: Boolean, explanationText: String) {
        val currentState = questionStates[currentQuestionIndex] ?: QuestionState(null, false, false)
        questionStates[currentQuestionIndex] = currentState.copy(
            selectedAnswer = answerText,
            isAnswerCorrect = isCorrect,
            isAnswerLocked = true
        )

        if (!isCorrect) {
            //  Добавляем или обновляем explanationText
            explanationTexts[currentQuestionIndex] = explanationText

            //  Обновляем отображение комментария каждый раз при ошибке
            questionCommentsState[currentQuestionIndex] = true

            // Добавляем в список неправильных вопросов, если ещё не был
            if (!incorrectQuestions.contains(currentQuestionIndex)) {
                incorrectQuestions.add(currentQuestionIndex)
            }
        }
        // Обновляем количество правильных ответов
        correctAnswersCount = questionStates.values.count { it.isAnswerCorrect }
        checkTestCompletion() //  Проверяем, завершён ли тест после ответа

    }

    fun getExplanationForQuestion(index: Int): String {
        return explanationTexts[index] ?: ""
    }




    fun getCurrentTicketNumber(questionList: List<Question>): String {
        val currentQuestion = questionList.getOrNull(currentQuestionIndex)
        return currentQuestion?.ticket_number ?: "Неизвестный билет - $currentQuestionIndex" // Логируем индекс вопроса
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

    // 🔥 Новый метод для сброса теста
    fun resetTest() {
        if (currentTicketStartIndex == 0) {
            currentTicketStartIndex = (currentQuestionIndex / 10) * 10 // ✅ Запоминаем текущий билет перед сбросом
        }

        questionStates.clear() // ✅ Очищаем все ответы
        correctAnswersCount = 0 // ✅ Сбрасываем количество правильных ответов
        isTestFinished = false // ✅ Сбрасываем флаг завершения теста
        currentQuestionIndex = currentTicketStartIndex // ✅ Перенаправляем на первый вопрос текущего билета

        // ✅ Очищаем комментарии
        questionCommentsState.clear()
        explanationTexts.clear()
    }


    fun loadRandomTicket() {
        questionStates.clear()
        questionCommentsState.clear() // ✅ очищаем комментарии
        explanationTexts.clear() // ✅ очищаем текст комментариев

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


    // Добавление билета в избранное
    fun addFavoriteTicket(ticketNumber: String) {
        _favoriteTickets.value = _favoriteTickets.value + ticketNumber
    }

    // Удаление билета из избранного
    fun removeFavoriteTicket(ticketNumber: String) {
        _favoriteTickets.value = _favoriteTickets.value - ticketNumber  // Удаляем из Set
    }


    // ✅ Функция для увеличения количества ошибок в режиме экзамена
    fun incrementExamWrongAnswers() {
        examWrongAnswersCount++
    }


    private val _ticketResults = MutableStateFlow<Map<String, Int>>(emptyMap())
    val ticketResults: StateFlow<Map<String, Int>> get() = _ticketResults

    init {
        viewModelScope.launch {
            favoriteTicketsManager.ticketResults.collect { results ->
                _ticketResults.value = results
            }
        }
    }

    fun saveTicketResult(ticketNumber: String, correctAnswers: Int) {
        viewModelScope.launch {
            favoriteTicketsManager.saveTicketResult(ticketNumber, correctAnswers)
        }
    }
    fun removeTicketResult(ticketNumber: String) {
        viewModelScope.launch {
            favoriteTicketsManager.removeTicketResult(ticketNumber)
        }
    }



}
