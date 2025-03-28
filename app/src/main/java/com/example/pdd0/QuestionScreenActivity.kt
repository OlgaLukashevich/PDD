package com.example.pdd0

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
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
import kotlin.math.roundToInt


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
                composable("question_screen/{questionIndex}/{screenRoute}") { backStackEntry ->
                    val questionIndex = backStackEntry.arguments?.getString("questionIndex")?.toIntOrNull() ?: 0
                    val screenRoute = backStackEntry.arguments?.getString("screenRoute") ?: "question_screen"
                    QuestionScreen(navController, questionIndex - 1, questionViewModel, screenRoute)
                }
                composable("all_questions_screen") {
                    AllQuestionsScreen(navController = navController, questionViewModel,questionList)
                }
                composable("favorite_question_screen") {
                    FavoriteQuestionScreen(navController, questionViewModel)
                }
            }
        }
    }
}

@Composable
fun QuestionScreen(navController: NavController, questionIndex: Int, viewModel: QuestionViewModel, screenRoute: String) {
    val questionList = parseJson(context = LocalContext.current) // Загрузка вопросов
    // ✅ Получаем количество правильных ответов из ViewModel
    val correctAnswersCount = viewModel.correctAnswersCount




    if (viewModel.isTestFinished) {
        LaunchedEffect(Unit) {
            navController.navigate("result_screen/$correctAnswersCount")
        }
        return
    }


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

    // ✅ Если тест завершён, переходим к экрану результата, передавая `correctAnswersCount`
    if (viewModel.isTestFinished) {
        LaunchedEffect(Unit) {
            navController.navigate("result_screen/$correctAnswersCount")
        }
        return
    }

    // Статус ответа
    var showExplanation by remember { mutableStateOf(false) } // Показывать пояснение
    var explanationText by remember { mutableStateOf("") }


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

                                // Если ответ неправильный, показываем подсказку
                                if (!answer.is_correct) {
                                    explanationText = currentQuestion.answer_tip
                                }
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

                        // Определение маршрута на основе текущего состояния
                        val nextScreenRoute =
                            if (screenRoute == "exam_screen") "exam_screen" else "question_screen"

                        // Переход с передачей параметров в соответствии с NavGraph
                        navController.navigate("question_screen/${viewModel.currentQuestionIndex}/$nextScreenRoute") {
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

        var offset by remember { mutableStateOf(Offset(0f, 0f)) }
        var showExplanation by remember { mutableStateOf(true) } // Состояние для отображения комментария


        // Показываем комментарий только если текущий вопрос неправильный
        if (viewModel.incorrectQuestions.contains(viewModel.currentQuestionIndex)) {
            // Получаем состояние комментария для конкретного вопроса
            val showExplanation = viewModel.getCommentStateForQuestion(viewModel.currentQuestionIndex)

            if (showExplanation) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) } // Используем offset для перемещения
                        .pointerInput(Unit) {
                            detectDragGestures { _, dragAmount ->
                                // Обновляем позицию подсказки при перетаскивании
                                offset = Offset(offset.x + dragAmount.x, offset.y + dragAmount.y)
                            }
                        }
                        .fillMaxWidth()
                        .align(Alignment.Center) // Поднимем подсказку чуть выше
                        .padding(16.dp)
                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(10))
                        .border(2.dp, Color.Red, RoundedCornerShape(10))
                        .padding(16.dp)
                ) {
                    Column {
                        // Кнопка закрытия комментария
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End // Располагаем крестик справа
                        ) {
                            IconButton(
                                onClick = {
                                    viewModel.hideCommentForQuestion(viewModel.currentQuestionIndex) // Скрываем комментарий только для этого вопроса
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Close",
                                    tint = Color.Red
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "Incorrect answer",
                                tint = Color.Red,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                Text(
                                    text = explanationText,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
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
}


