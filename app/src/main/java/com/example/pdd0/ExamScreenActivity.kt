package com.example.pdd0

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.pdd0.dataClass.Question
import com.example.pdd0.parser.parseJson
import com.example.pdd0.utils.AnswerButton
import com.example.pdd0.utils.QuestionNavigationPanel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ExamScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val questionViewModel: QuestionViewModel = viewModel()
            val questionList = parseJson(LocalContext.current)

            NavHost(navController = navController, startDestination = "main_screen") {
                composable("main_screen") { MainScreen(navController, questionViewModel, questionList) }

                composable("exam_screen/{questionIndex}") { backStackEntry ->
                    val questionIndex = backStackEntry.arguments?.getString("questionIndex")?.toIntOrNull() ?: 0
                    ExamScreen(navController, questionIndex, questionViewModel)
                }

                composable("all_questions_screen") {
                    AllQuestionsScreen(navController, questionViewModel, questionList)
                }

                composable("favorite_question_screen") {
                    FavoriteQuestionScreen(navController, questionViewModel)
                }
            }
        }
    }
}


@Composable
fun ExamScreen(navController: NavController, questionIndex: Int, viewModel: QuestionViewModel) {
    val questionList = parseJson(context = LocalContext.current) // Загружаем вопросы

    // ✅ Таймер на 15 минут
    val timerMillis = 3 * 60 * 1000L
    var timeLeft by remember { mutableStateOf(timerMillis) }
    var isTimeUp by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Запуск таймера
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            while (timeLeft > 0) {
                delay(1000L)
                timeLeft -= 1000L
            }
            isTimeUp = true
        }
    }

    // Если время вышло → переходим на экран результатов
    if (isTimeUp) {
        LaunchedEffect(Unit) {
            navController.navigate("result_screen/${viewModel.correctAnswersCount}")
        }
        return
    }

    // При изменении индекса загружаем состояние вопроса
    LaunchedEffect(questionIndex) {
        if (viewModel.currentQuestionIndex != questionIndex) {
            viewModel.saveCurrentQuestionState()
            viewModel.currentQuestionIndex = questionIndex
            viewModel.loadQuestionState()
        }
    }

    val currentQuestion = questionList.getOrNull(viewModel.currentQuestionIndex)
    if (currentQuestion == null) {
        Text(text = "Ошибка загрузки вопроса", fontSize = 24.sp)
        return
    }

    val correctAnswersCount = viewModel.correctAnswersCount

    // ✅ Если все вопросы пройдены → переходим на результат
    if (viewModel.isTestFinished) {
        LaunchedEffect(Unit) {
            navController.navigate("result_screen/$correctAnswersCount")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ✅ Отображение таймера
        Text(
            text = formatTime(timeLeft),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (timeLeft < 60_000L) Color.Red else Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Панель навигации
        QuestionNavigationPanel(navController, viewModel)

        Spacer(modifier = Modifier.height(22.dp))


        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Отображение вариантов ответов
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            currentQuestion.answers.forEach { answer ->
                val questionState = viewModel.getCurrentQuestionState()
                val isSelected = questionState.selectedAnswer == answer.answer_text
                val isCorrect = questionState.isAnswerCorrect

                AnswerButton(
                    answerText = answer.answer_text,
                    isCorrect = isCorrect,
                    isSelected = isSelected,
                    onClick = {
                        if (!questionState.isAnswerLocked) {
                            viewModel.saveAnswer(answer.answer_text, answer.is_correct)
                        }
                    },
                    isAnswerCorrect = isCorrect
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Навигация "Назад" и "Вперёд"
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // 🔙 Кнопка "Назад"
            IconButton(
                onClick = {
                    if (viewModel.currentQuestionIndex > 0) {
                        viewModel.saveCurrentQuestionState()
                        viewModel.currentQuestionIndex--
                    }
                },
                enabled = viewModel.currentQuestionIndex > 0
            ) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Previous")
            }

            // ✅ Если тест завершён → кнопка завершения
            if (viewModel.allQuestionsAnswered()) {
                IconButton(
                    onClick = {
                        navController.navigate("result_screen/${viewModel.correctAnswersCount}")
                    }
                ) {
                    Icon(imageVector = Icons.Filled.Check, contentDescription = "Finish")
                }
            } else {
                // 🔜 Кнопка "Вперёд"
                IconButton(
                    onClick = {
                        viewModel.saveCurrentQuestionState()
                        viewModel.moveToNextQuestion()
                        navController.navigate("exam_screen/${viewModel.currentQuestionIndex}") {
                            launchSingleTop = true
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = "Next")
                }
            }
        }
    }
}


// Функция для форматирования оставшегося времени (минуты:секунды)
fun formatTime(millis: Long): String {
    val minutes = (millis / 1000) / 60
    val seconds = (millis / 1000) % 60
    return String.format("%02d:%02d", minutes, seconds)
}
