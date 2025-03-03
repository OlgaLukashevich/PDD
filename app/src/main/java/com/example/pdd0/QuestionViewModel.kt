package com.example.pdd0

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.*
class QuestionViewModel : ViewModel() {
    var currentQuestionIndex by mutableStateOf(0)
    var isTestFinished by mutableStateOf(false)
    var questionStates = mutableStateMapOf<Int, QuestionState>()

    var selectedAnswer by mutableStateOf<String?>(null)
    var isAnswerCorrect by mutableStateOf(false)

    fun getCurrentQuestionState(): QuestionState {
        return questionStates[currentQuestionIndex] ?: QuestionState(null, false, false)
    }

//    fun saveAnswer(answerText: String, isCorrect: Boolean) {
//        selectedAnswer = answerText
//        isAnswerCorrect = isCorrect
//        questionStates[currentQuestionIndex] = QuestionState(answerText, isCorrect, true)
//    }

    fun saveAnswer(answerText: String, isCorrect: Boolean) {
        val currentState = questionStates[currentQuestionIndex] ?: QuestionState(null, false, false)
        questionStates[currentQuestionIndex] = currentState.copy(
            selectedAnswer = answerText,
            isAnswerCorrect = isCorrect,
            isAnswerLocked = true
        )
    }

    fun loadQuestionState() {
//        val currentState = getCurrentQuestionState()
//        selectedAnswer = currentState.selectedAnswer
//        isAnswerCorrect = currentState.isAnswerCorrect
    }

    fun saveCurrentQuestionState() {
//        questionStates[currentQuestionIndex] = QuestionState(selectedAnswer, isAnswerCorrect, true)
//    }
//}
        // Просто сохраняем текущий вопрос, не изменяя его
        if (!questionStates.containsKey(currentQuestionIndex)) {
            questionStates[currentQuestionIndex] = QuestionState(null, false, false)
        }
    }
}
