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
import com.example.pdd0.parser.parseJson
import kotlinx.coroutines.delay

class QuestionScreenActivity : ComponentActivity() {
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
                    QuestionScreen(navController, questionIndex - 1, questionViewModel) // Передаем ViewModel
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
            if (!currentQuestion.image.isNullOrEmpty() && currentQuestion.image.trim().isNotEmpty()) {
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
            }
            else {
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
                    enabled = viewModel.currentQuestionIndex > 0
                ) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Previous")
                }

            // Если все вопросы отвечены, показываем галочку
            if (viewModel.allQuestionsAnswered()) {
                IconButton(
                    onClick = {
                        navController.navigate("result_screen/${viewModel.correctAnswersCount}")
                    }
                ) {
                    Icon(imageVector = Icons.Filled.Check, contentDescription = "Finish")
                }
            } else {
                // 🔥 Правильный переход "Вперёд"
                IconButton(
                    onClick = {
                        viewModel.saveCurrentQuestionState()
                        viewModel.moveToNextQuestion()
                        navController.navigate("question_screen/${viewModel.currentQuestionIndex}") {
                            launchSingleTop = true // ✅ Избегаем дублирования экранов
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = "Next")
                }
            }



        }


    }


    // Диалоговое окно для увеличенной картинки
    if (isImageFullScreen) {
        Dialog(onDismissRequest = { isImageFullScreen = false }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black) // Затемнённый фон
                    .clickable { isImageFullScreen = false } // Закрытие при нажатии
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



@Composable
fun QuestionNavigationPanel(navController: NavController, viewModel: QuestionViewModel) {
    var isPaused by remember { mutableStateOf(false) } // Отслеживаем состояние паузы
    var showPauseDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current // Получаем контекст

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Кнопка Play/Pause
        IconButton(onClick = {
            isPaused = !isPaused // Переключаем состояние
            showPauseDialog = isPaused // Показываем диалог только при нажатии на паузу
        }) {
            Icon(
                imageVector = if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                contentDescription = if (isPaused) "Play" else "Pause"
            )
        }

        val baseIndex = (viewModel.currentQuestionIndex / 10) * 10 // Определяем первый вопрос текущего билета

        ( 0..9).forEach { offset ->
            val questionIndex = baseIndex + offset
            val questionState = viewModel.questionStates[questionIndex]

            val color = when {
                viewModel.currentQuestionIndex == questionIndex -> Color.Black  // Текущий вопрос
                questionState?.selectedAnswer == null -> Color.Gray  // Не отвечен
                questionState.isAnswerCorrect -> Color.Green        // Правильный ответ
                else -> Color.Red                                   // Неправильный ответ
            }

            Text(
                text = "${offset + 1}",
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(4.dp)
                    .clickable {
                        viewModel.saveCurrentQuestionState()
                        if (viewModel.currentQuestionIndex != questionIndex) {
                            viewModel.currentQuestionIndex = questionIndex
                            viewModel.loadQuestionState()
                        }

                        navController.navigate("question_screen/$questionIndex") {
                            launchSingleTop = true
                        }
                    },
                color = color,
                fontWeight = if (viewModel.currentQuestionIndex == questionIndex) FontWeight.Bold else FontWeight.Normal
            )
        }
    }


    // Диалог с вариантами действий
    if (showPauseDialog) {

        PauseDialog(
            navController = navController, // Передаем navController
            viewModel = viewModel, // ✅ Передаём ViewModel для кнопки "Следующий случайный билет"
            onResume = {
                showPauseDialog = false
                isPaused = false // Автоматически меняем иконку на паузу при закрытии диалога
            },
            onGoHome = {
                showPauseDialog = false
                isPaused = false // Возвращаем плей при переходе на главную
                navController.navigate("main_screen") // Переход на главный экран
            },
            onAddToFavorites = {
                viewModel.toggleFavoriteTicket(viewModel.currentQuestionIndex.toString()) // ✅ Исправлено
//                val message = if (viewModel.isTicketFavorite) {
//                    "Билет добавлен в избранное"
//                } else {
//                    "Билет удален из избранного"
//                }
  //              Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            },
            currentTicketNumber = viewModel.currentQuestionIndex.toString()
        )
    }
}



data class QuestionState(
    val selectedAnswer: String?,
    val isAnswerCorrect: Boolean,
    val isAnswerLocked: Boolean
)

@Composable
fun PauseDialog(

    navController: NavController, // Добавляем NavController
    viewModel: QuestionViewModel, // ✅ Добавляем ViewModel для управления билетами
    onResume: () -> Unit,
    onGoHome: () -> Unit,
    onAddToFavorites: () -> Unit,
    currentTicketNumber: String // Номер текущего билета

) {


    val context = LocalContext.current
    val favoriteTickets by viewModel.favoriteTickets.collectAsState() // ✅ Исправлено!
    val isFavorite = favoriteTickets.contains(currentTicketNumber)


    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(text = "Пауза")
        },
        text = {
            Column {
                TextButton(onClick = onResume) {
                    Text("Продолжить")
                }
                TextButton(onClick = {
                    navController.navigate("main_screen") // Переход на главный экран
                }) {
                    Text("На главную")
                }
                // Кнопка добавления в избранное
                TextButton(onClick = {
                    Log.d("PauseDialog", "Добавляю билет в избранное: $currentTicketNumber") // ✅ Логируем
                    onAddToFavorites()
                    onResume()
                }) {
                    Text(if (isFavorite) "Удалить из избранного" else "Добавить в избранное")
                }

                TextButton(onClick = {
                    viewModel.loadRandomTicket()
                    navController.navigate("question_screen/${viewModel.currentQuestionIndex}") {
                        popUpTo("main_screen") { inclusive = false }
                    }
                }) {
                    Text("Следующий случайный билет(пройти заново не работает)")
                }

            }
        },
        confirmButton = {
            TextButton(onClick = onResume) {
                Text("Закрыть")
            }
        }
    )
}

@Composable
fun AnswerButton(
    answerText: String,
    isCorrect: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    isAnswerCorrect: Boolean
) {
    val backgroundColor = when {
        isSelected && isAnswerCorrect -> Color.Green
        isSelected && !isAnswerCorrect -> Color.Red
        else -> Color.Gray
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor) // Исправили на containerColor
    ) {
        Text(
            text = answerText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}