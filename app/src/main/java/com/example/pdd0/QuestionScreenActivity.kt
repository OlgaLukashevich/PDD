package com.example.pdd0

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.pdd0.parser.parseJson
import com.example.pdd0.utils.QuestionNavigationPanel
import com.example.pdd0.utils.AnswerButton



class QuestionScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Навигация
            val navController = rememberNavController()
            val questionViewModel: QuestionViewModel = viewModel() // Создаём ViewModel
            val questionList = parseJson(LocalContext.current) // Загружаем список вопросов

            NavHost(navController = navController, startDestination = "main_screen") {
                composable("main_screen") { MainScreen(navController, questionViewModel,questionList) }
                composable("question_screen/{questionIndex}") { backStackEntry ->
                // Извлекаем индекс вопроса из аргументов
                    val questionIndex = backStackEntry.arguments?.getString("questionIndex")?.toIntOrNull() ?: 1
                    QuestionScreen(navController, questionIndex - 1, questionViewModel) // Передаем ViewModel
                }
                composable("all_questions_screen") {
                    AllQuestionsScreen(navController = navController, questionViewModel,questionList) // ✅ Передаём viewModel
                }
                composable("favorite_question_screen") {
                    FavoriteQuestionScreen(navController, questionViewModel)
                }
            }
        }
    }
}

@Composable
fun QuestionScreen(navController: NavController, questionIndex: Int, viewModel: QuestionViewModel) {
    val questionList = parseJson(context = LocalContext.current) // Загрузка вопросов


    // При изменении индекса загружаем состояние ВьюМодели
    LaunchedEffect(questionIndex) {
        if (viewModel.currentQuestionIndex != questionIndex) {
            viewModel.saveCurrentQuestionState() // Сначала сохраняем ответ текущего вопроса
            viewModel.currentQuestionIndex = questionIndex // Обновляем индекс
            viewModel.loadQuestionState() // Загружаем сохранённое состояние
        }
    }

    var isImageFullScreen by remember { mutableStateOf(false) } // Отслеживаем увеличение картинки
    // Переход от индекса к конкретному вопросу
    val currentQuestion = questionList.getOrNull(viewModel.currentQuestionIndex)
    // Если данных нет, показываем заглушку
    if (currentQuestion == null) {
        Text(text = "Ошибка загрузки вопроса", fontSize = 24.sp)
        return
    }
    // ✅ Получаем количество правильных ответов из ViewModel
    val correctAnswersCount = viewModel.correctAnswersCount

    // ✅ Если тест завершён, переходим к экрану результата, передавая `correctAnswersCount`
    if (viewModel.isTestFinished) {
        LaunchedEffect(Unit) {
            navController.navigate("result_screen/$correctAnswersCount")
        }
        return
    }

    // Статус ответа
    var showFeedback by remember { mutableStateOf(false) } // Показывать подсказку и тему

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Фоновое изображение
        Image(
            painter = painterResource(id = R.drawable.question_background), // Замените на ваш ресурс изображения
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Масштабирование изображения
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Заголовок с номером билета
            Text(
                text = "${currentQuestion.ticket_number}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Панель навигации
            QuestionNavigationPanel(navController, viewModel)
            //QuestionNavigationPanel(navController, viewModel, screenRoute = "exam_screen")

            Spacer(modifier = Modifier.height(22.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()

                    .height(230.dp),  // Увеличиваем высоту, чтобы уместить и текст, и изображение
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Вопрос
                Text(
                    text = currentQuestion.question,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))
                // Выводим изображение вопроса, если оно есть
                if (!currentQuestion.image.isNullOrEmpty() && currentQuestion.image.trim()
                        .isNotEmpty()
                ) {
                    val context = LocalContext.current

                    try {
                        val inputStream = context.assets.open(currentQuestion.image)
                        Log.d("ImageCheck", "Файл найден: ${currentQuestion.image}")
                    } catch (e: Exception) {
                        Log.e("ImageCheck", "Файл не найден: ${currentQuestion.image}", e)
                    }

                    val imagePainter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(context)
                            .data("file:///android_asset/${currentQuestion.image}")
                            .build()
                    )

                    Image(
                        painter = imagePainter,
                        contentDescription = "Image for question",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(16.dp)
                            .clickable { isImageFullScreen = true }, // Нажатие для увеличения
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Log.d("ImageCheck", "Изображение отсутствует, пропускаем загрузку")
                }

            }

            // 🔥 Оборачиваем ответы в `Column(Modifier.weight(1f))`, чтобы кнопки не сдвигались
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()), // ✅ Добавляем прокрутку
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ответы
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
                             //   showFeedback = true // Показываем подсказки и тему
                            }
                        },
                        isAnswerCorrect = isCorrect
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            // 🔥 Фиксируем кнопки "Назад" и "Вперед" внизу экрана
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
                        .background(Color.Gray.copy(alpha = 0.4f),  shape = RoundedCornerShape(10)) // Полупрозрачная обводка
                        .padding(4.dp) // Паддинг вокруг иконки
                ) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Previous")
                }
                // Кнопка "Завершить тест"
                IconButton(
                    onClick = {
                        // Переход на экран с результатами
                        navController.navigate("result_screen/${viewModel.correctAnswersCount}")
                    },
                    modifier = Modifier
                        .background(Color.Gray.copy(alpha = 0.4f),  shape = RoundedCornerShape(10))
                        .padding(4.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Check, contentDescription = "Finish Test")
                }

                // Кнопка "Вперед"
                IconButton(
                    onClick = {
                        viewModel.saveCurrentQuestionState()
                        viewModel.moveToNextQuestion()
                        navController.navigate("question_screen/${viewModel.currentQuestionIndex}") {
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


        // Показываем комментарий после ответа
        if (showFeedback) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                // Комментарий по ответу
                Text(
                    text = "Комментарий: ${currentQuestion.answer_tip}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Тема вопроса
                Text(
                    text = "Тема: ${currentQuestion.topic.joinToString(", ")}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }

        // Диалоговое окно для увеличенной картинки
        if (isImageFullScreen) {
            Dialog(onDismissRequest = { isImageFullScreen = false }) {
                val scaleState = remember { mutableStateOf(1f) } // Хранение масштаба изображения

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .clickable { isImageFullScreen = false } // Закрытие при нажатии на фон
                ) {
                    val imagePainter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("file:///android_asset/${currentQuestion.image}")
                            .build()
                    )

                    // Обработчик жестов для увеличения картинки
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scaleState.value *= zoom // Масштабируем картинку по жесту
                                }
                            }
                    ) {
                        IconButton(
                            onClick = { isImageFullScreen = false },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }

                        Image(
                            painter = imagePainter,
                            contentDescription = "Full-screen image",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scaleState.value,
                                    scaleY = scaleState.value
                                ) // Применяем масштабирование
                                .padding(16.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }

    }


