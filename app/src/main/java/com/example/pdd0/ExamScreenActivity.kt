package com.example.pdd0

import android.net.Uri
import android.os.Bundle
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
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

                composable("exam_screen/{questionIndex}/{screenRoute}") { backStackEntry ->
                    val questionIndex = backStackEntry.arguments?.getString("questionIndex")?.toIntOrNull() ?: 0
                    val screenRoute = backStackEntry.arguments?.getString("screenRoute") ?: "exam_screen"
                    ExamScreen(navController, questionIndex, questionViewModel, screenRoute)
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
fun ExamScreen(navController: NavController, questionIndex: Int, viewModel: QuestionViewModel, screenRoute: String) {
    val timeLeft by viewModel.timeLeft.observeAsState(initial = 3 * 60 * 1000L)
    val questionList = parseJson(context = LocalContext.current) // Загружаем вопросы

    // Запуск таймера только при переходе на экран экзамена
    LaunchedEffect(Unit) {
        viewModel.resumeTimer()  // Таймер возобновляется при переходе на экран экзамена
       // viewModel.resetExamCounters()
    }

    // Таймер
    val formattedTime = formatTime(timeLeft)




// Если время вышло или допущено 2 ошибки → завершаем тест
    if (viewModel.isTimeUp || viewModel.examWrongAnswersCount >= 2) {
        LaunchedEffect(Unit) {
            // Сбрасываем счетчик ошибок и таймер перед переходом на экран с результатами
            viewModel.resetExamCounters()

            // Переход на экран результатов, при этом удаляя экран экзамена из стека
            navController.navigate("result_screen/${viewModel.correctAnswersCount}") {
                popUpTo("exam_screen") { inclusive = true } // Удаление экзамена из стека
            }
        }
        return
    }




    val currentQuestion = questionList.getOrNull(viewModel.currentQuestionIndex)
    if (currentQuestion == null) {
        Text(text = "Ошибка загрузки вопроса", fontSize = 24.sp)
        return
    }
    var isImageFullScreen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Фоновое изображение
        Image(
            painter = painterResource(id = R.drawable.question_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Заголовок с таймером
            Text(
                text = "Время: $formattedTime",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (timeLeft < 60_000L) Color.Red else Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))
            // Панель навигации
            //QuestionNavigationPanel(navController, viewModel)
            QuestionNavigationPanel(navController, viewModel, screenRoute = "exam_screen")

            Spacer(modifier = Modifier.height(8.dp))


            // Вопрос и варианты ответов
            Text(text = currentQuestion.question, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Выводим изображение вопроса, если оно есть
            if (!currentQuestion.image.isNullOrEmpty() && currentQuestion.image.trim()
                    .isNotEmpty()
            ) {
                val context = LocalContext.current
                val imagePainter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(context)
                        .data(Uri.parse("file:///android_asset/${currentQuestion.image}"))
                        .build()
                )

                Image(
                    painter = imagePainter,
                    contentDescription = "Image for question",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp)
                        .clickable { isImageFullScreen = true },
                    contentScale = ContentScale.Fit
                )
            }

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

                                // ✅ Отдельный метод для учета ошибок в экзамене
                                if (!answer.is_correct) {
                                    viewModel.incrementExamWrongAnswers()
                                }
                            }
                        },
                        isAnswerCorrect = isCorrect
                    )
                }
            }



            Spacer(modifier = Modifier.height(16.dp))

            // Навигация "Назад" и "Вперёд"
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // 🔥 Кнопка "Назад"
                IconButton(
                    onClick = {
                        if (viewModel.currentQuestionIndex > 0) {
                            viewModel.saveCurrentQuestionState()
                            viewModel.currentQuestionIndex--
                        }
                    },
                    enabled = viewModel.currentQuestionIndex > 0,
                    modifier = Modifier
                        .background(
                            Color.Gray.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(10)
                        ) // Полупрозрачная обводка
                        .padding(4.dp) // Паддинг вокруг иконки
                ) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Previous")
                }
                // Кнопка "Завершить тест"
                IconButton(
                    onClick = {
                        // Переход на экран с результатами
                        navController.navigate("result_screen/${viewModel.correctAnswersCount}")
                        viewModel.resetTimerToInitial()
                    },
                    modifier = Modifier
                        .background(Color.Gray.copy(alpha = 0.4f), shape = RoundedCornerShape(10))
                        .padding(4.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Check, contentDescription = "Finish Test")
                }

                // Кнопка "Вперед"
                IconButton(
                    onClick = {
                        viewModel.saveCurrentQuestionState()
                        viewModel.moveToNextQuestion()
                        navController.navigate("exam_screen/${viewModel.currentQuestionIndex}/exam_screen") { // Передаем оба параметра
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier
                        .background(Color.Gray.copy(alpha = 0.4f), shape = RoundedCornerShape(10))
                        .padding(4.dp)
                ) {
                    Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = "Next")
                }
            }

        }
    }
}

// Функция для форматирования оставшегося времени
fun formatTime(millis: Long): String {
    val minutes = (millis / 1000) / 60
    val seconds = (millis / 1000) % 60
    return String.format("%02d:%02d", minutes, seconds)
}
