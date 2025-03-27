package com.example.pdd0.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pdd0.QuestionViewModel
import com.example.pdd0.parser.parseJson

@Composable
fun QuestionNavigationPanel(
    navController: NavController, viewModel: QuestionViewModel, screenRoute: String = "question_screen"

//    screenRoute: String = "question_screen" // ✅ Дефолтное значение - обычный режим
) {
    var isPaused by remember { mutableStateOf(false) } // Отслеживаем состояние паузы
    var showPauseDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current // Получаем контекст


    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Pause/Play button
        IconButton(
            onClick = {
                isPaused = !isPaused // Переключаем состояние
                if (isPaused) {
                    viewModel.pauseTimer() // Пауза таймера
                    showPauseDialog = true // Показываем диалог с паузой
                } else {
                    viewModel.resumeTimer() // Возобновляем таймер
                    showPauseDialog = false // Скрываем диалог
                }
            }
        ) {
            Icon(
                imageVector = if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                contentDescription = if (isPaused) "Play" else "Pause"
            )
        }

        val baseIndex =
            (viewModel.currentQuestionIndex / 10) * 10 // Определяем первый вопрос текущего билета

        (0..9).forEach { offset ->
            val questionIndex = baseIndex + offset
            val questionState = viewModel.questionStates[questionIndex]

            val color = when {
                viewModel.currentQuestionIndex == questionIndex -> Color.Black  // Текущий вопрос
                questionState?.selectedAnswer == null -> Color.Gray  // Не отвечен
                questionState.isAnswerCorrect -> Color.Green        // Правильный ответ
                else -> Color.Red                                   // Неправильный ответ
            }
            // Обернуть текст в Box с фоном
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .clickable {
                        viewModel.saveCurrentQuestionState()

                        // Проверяем, если мы находимся на экране экзамена, то переходим на exam_screen, иначе остаёмся на question_screen
                        if (viewModel.currentQuestionIndex != questionIndex) {
                            viewModel.currentQuestionIndex = questionIndex
                            viewModel.loadQuestionState()
                        }

                        // Проверяем, если текущий экран exam_screen, то переходим на exam_screen
                        val nextScreenRoute = if (screenRoute == "exam_screen") "exam_screen" else "question_screen"

                        // Навигация с передачей индекса вопроса и экрана
                        navController.navigate("$nextScreenRoute/$questionIndex/$screenRoute") {
                            launchSingleTop = true
                        }
                    }
                    .background(
                        color.copy(alpha = 0.5f), // Полупрозрачный фон
                        shape = RoundedCornerShape(10) // Круглый фон
                    )
                    .padding(6.dp) // Отступы внутри
            ) {
                Text(
                    text = "${offset + 1}",
                    fontSize = 18.sp,
                    color = Color.White, // Белый цвет текста для контраста
                    fontWeight = if (viewModel.currentQuestionIndex == questionIndex) FontWeight.Bold else FontWeight.Normal
                )
            }

        }
    }
            // Диалог с вариантами действий
    if (showPauseDialog) {
        val context = LocalContext.current
        val questionList = parseJson(context) // ✅ Загружаем список вопросов

        PauseDialog(
            navController = navController, // Передаем navController
            viewModel = viewModel, // ✅ Передаём ViewModel для кнопки "Следующий случайный билет"
            onResume = {
                showPauseDialog = false
                isPaused = false // Автоматически меняем иконку на паузу при закрытии диалога
                viewModel.resumeTimer() // Возобновляем таймер
            },
            onGoHome = {
                showPauseDialog = false
                isPaused = false // Возвращаем плей при переходе на главную
                navController.navigate("main_screen") // Переход на главный экран
            },
            onAddToFavorites = {
                val ticketNumber = viewModel.getCurrentTicketNumber(questionList) // ✅ Теперь берём номер из данных
                viewModel.toggleFavoriteTicket(ticketNumber)
            },
            currentTicketNumber = viewModel.getCurrentTicketNumber(questionList), // ✅ Передаём корректный номер
            questionList = questionList // ✅ Теперь передаём список вопросов
        )
    }
}

