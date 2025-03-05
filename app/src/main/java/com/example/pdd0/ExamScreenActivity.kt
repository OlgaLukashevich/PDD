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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.pdd0.parser.parseJson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ExamScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Навигация
            val navController = rememberNavController()
            val questionViewModel: QuestionViewModel = viewModel() // Создаём ViewModel
            val questionList = parseJson(LocalContext.current) // Загружаем список вопросов

            NavHost(navController = navController, startDestination = "main_screen") {
                composable("main_screen") { MainScreen(navController, questionViewModel) }
                composable("question_screen/{questionIndex}") { backStackEntry ->
                    // Извлекаем индекс вопроса из аргументов
                    val questionIndex = backStackEntry.arguments?.getString("questionIndex")?.toIntOrNull() ?: 1
                    ExamScreen(navController,  questionViewModel) // Передаем ViewModel
                }
                composable("all_questions_screen") {
                    AllQuestionsScreen(navController = navController, viewModel = questionViewModel) // ✅ Передаём viewModel
                }
                composable("favorite_question_screen") {
                    FavoriteQuestionScreen(navController, questionViewModel)
                }
            }
        }
    }
}
@Composable
fun ExamScreen(navController: NavController, viewModel: QuestionViewModel) {
    val questionList = parseJson(context = LocalContext.current) // Загрузка вопросов
    val timerMillis = 15 * 60 * 1000L // 15 минут в миллисекундах
    var timeLeft by remember { mutableStateOf(timerMillis) } // Состояние таймера
    var isTimeUp by remember { mutableStateOf(false) } // Флаг завершения времени
    val coroutineScope = rememberCoroutineScope()

    // Запускаем таймер при входе в экран
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            while (timeLeft > 0) {
                delay(1000L) // Ждем 1 секунду
                timeLeft -= 1000L
            }
            isTimeUp = true // Время истекло, показываем результаты
        }
    }

    // Когда время истекло, автоматически переходим к результатам
    if (isTimeUp) {
        LaunchedEffect(Unit) {
            navController.navigate("result_screen/${viewModel.correctAnswersCount}")
        }
        return
    }

    var isImageFullScreen by remember { mutableStateOf(false) } // Для увеличения изображения
    val currentQuestion = questionList.getOrNull(viewModel.currentQuestionIndex)

    if (currentQuestion == null) {
        Text(text = "Ошибка загрузки вопроса", fontSize = 24.sp)
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ✅ Таймер в верхней части
        Text(
            text = formatTime(timeLeft),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (timeLeft < 60_000L) Color.Red else Color.Black // Если меньше минуты - красный
        )
        Spacer(modifier = Modifier.height(16.dp))

        QuestionNavigationPanel(navController, viewModel) // Навигационная панель
        Spacer(modifier = Modifier.height(22.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentQuestion.question,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!currentQuestion.image.isNullOrEmpty()) {
                val context = LocalContext.current
                val imagePainter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(context)
                        .data("file:///android_asset/${currentQuestion.image}")
                        .build()
                )

                Image(
                    painter = imagePainter,
                    contentDescription = "Question Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable { isImageFullScreen = true },
                    contentScale = ContentScale.Fit
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
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

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
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

            if (viewModel.allQuestionsAnswered()) {
                IconButton(
                    onClick = {
                        navController.navigate("result_screen/${viewModel.correctAnswersCount}")
                    }
                ) {
                    Icon(imageVector = Icons.Filled.Check, contentDescription = "Finish")
                }
            } else {
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

    if (isImageFullScreen) {
        Dialog(onDismissRequest = { isImageFullScreen = false }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { isImageFullScreen = false }
            ) {
                val imagePainter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("file:///android_asset/${currentQuestion.image}")
                        .build()
                )

                IconButton(
                    onClick = { isImageFullScreen = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                }

                Image(
                    painter = imagePainter,
                    contentDescription = "Full-screen image",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )
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
