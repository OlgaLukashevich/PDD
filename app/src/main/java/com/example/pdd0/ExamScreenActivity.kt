//package com.example.pdd0
//
//import android.os.Bundle
//import android.util.Log
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.ArrowForward
//import androidx.compose.material.icons.filled.Check
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material.icons.filled.Pause
//import androidx.compose.material.icons.filled.PlayArrow
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.window.Dialog
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import coil.compose.rememberAsyncImagePainter
//import coil.request.ImageRequest
//import com.example.pdd0.dataClass.Question
//import com.example.pdd0.parser.parseJson
//import com.example.pdd0.utils.QuestionNavigationPanel
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//
//class ExamScreenActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            val navController = rememberNavController()
//            val questionViewModel: QuestionViewModel = viewModel()
//            val questionList = parseJson(LocalContext.current)
//
//            NavHost(navController = navController, startDestination = "main_screen") {
//                composable("main_screen") { MainScreen(navController, questionViewModel, questionList) }
//
//                // ✅ Исправленный маршрут для ExamScreen
//                composable("exam_screen/{questionIndex}") { backStackEntry ->
//                    val questionIndex = backStackEntry.arguments?.getString("questionIndex")?.toIntOrNull() ?: 0
//                    viewModel.currentQuestionIndex = questionIndex // ✅ Гарантируем правильный индекс
//                    ExamScreen(navController, viewModel)
//                }
//
//                composable("all_questions_screen") {
//                    AllQuestionsScreen(navController = navController, questionViewModel, questionList)
//                }
//
//                composable("favorite_question_screen") {
//                    FavoriteQuestionScreen(navController, questionViewModel)
//                }
//            }
//        }
//    }
//}
//
//
//@Composable
//fun ExamScreen(navController: NavController, viewModel: QuestionViewModel) {
//    val questionList = parseJson(context = LocalContext.current) // Загружаем вопросы
//    val timerMillis = 3 * 60 * 1000L // 15 минут в миллисекундах
//    var timeLeft by remember { mutableStateOf(timerMillis) } // Состояние таймера
//    var isTimeUp by remember { mutableStateOf(false) } // Флаг завершения времени
//    val coroutineScope = rememberCoroutineScope()
//
//    // Запускаем таймер при входе
//    LaunchedEffect(Unit) {
//        coroutineScope.launch {
//            while (timeLeft > 0) {
//                delay(1000L) // Ждём 1 секунду
//                timeLeft -= 1000L
//            }
//            isTimeUp = true // Время истекло
//        }
//    }
//
//    // Если время истекло → показываем результат
//    if (isTimeUp) {
//        LaunchedEffect(Unit) {
//            navController.navigate("result_screen/${viewModel.correctAnswersCount}")
//        }
//        return
//    }
//
//
//
//    // Определяем текущий вопрос
//    val currentQuestion = questionList.getOrNull(viewModel.currentQuestionIndex)
//
//    if (currentQuestion == null) {
//        Text(text = "Ошибка загрузки вопроса", fontSize = 24.sp)
//        return
//    }
//
//    // Получаем номер билета
//    val currentTicketNumber = viewModel.getCurrentTicketNumber(questionList)
//    val favoriteTickets by viewModel.favoriteTickets.collectAsState()
//    val isFavorite = favoriteTickets.contains(currentTicketNumber)
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        // ✅ Отображение таймера
//        Text(
//            text = formatTime(timeLeft),
//            fontSize = 24.sp,
//            fontWeight = FontWeight.Bold,
//            color = if (timeLeft < 60_000L) Color.Red else Color.Black
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // ✅ Панель навигации
//        QuestionNavigationPanel(navController, viewModel)
//        //QuestionNavigationPanel(navController, viewModel, screenRoute = "exam_screen")
//
//        Spacer(modifier = Modifier.height(22.dp))
//
//        // ✅ Отображение вопроса и изображения (используем логику из `QuestionScreen`)
//        QuestionContent(currentQuestion)
//
//        // ✅ Кнопка "Добавить в избранное"
//        Button(onClick = {
//            viewModel.toggleFavoriteTicket(currentTicketNumber)
//        }) {
//            Text(if (isFavorite) "Удалить из избранного" else "Добавить в избранное")
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // ✅ Навигация "Назад" и "Вперёд"
//        ExamNavigationControls(navController, viewModel)
//    }
//}
//
//
//@Composable
//fun QuestionContent(currentQuestion: Question) {
//    var isImageFullScreen by remember { mutableStateOf(false) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(230.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            text = currentQuestion.question,
//            fontSize = 20.sp,
//            fontWeight = FontWeight.Bold
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // ✅ Проверяем, есть ли изображение
//        if (!currentQuestion.image.isNullOrEmpty()) {
//            val context = LocalContext.current
//            val imagePainter = rememberAsyncImagePainter(
//                model = ImageRequest.Builder(context)
//                    .data("file:///android_asset/${currentQuestion.image}")
//                    .build()
//            )
//
//            Image(
//                painter = imagePainter,
//                contentDescription = "Question Image",
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(200.dp)
//                    .clickable { isImageFullScreen = true },
//                contentScale = ContentScale.Fit
//            )
//        }
//    }
//
//    // ✅ Окно с увеличенным изображением
//    if (isImageFullScreen) {
//        Dialog(onDismissRequest = { isImageFullScreen = false }) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color.Black)
//                    .clickable { isImageFullScreen = false }
//            ) {
//                val imagePainter = rememberAsyncImagePainter(
//                    model = ImageRequest.Builder(LocalContext.current)
//                        .data("file:///android_asset/${currentQuestion.image}")
//                        .build()
//                )
//
//                IconButton(
//                    onClick = { isImageFullScreen = false },
//                    modifier = Modifier
//                        .align(Alignment.TopEnd)
//                        .padding(16.dp)
//                ) {
//                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
//                }
//
//                Image(
//                    painter = imagePainter,
//                    contentDescription = "Full-screen image",
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(16.dp),
//                    contentScale = ContentScale.Fit
//                )
//            }
//        }
//    }
//}
//
//
//@Composable
//fun ExamNavigationControls(navController: NavController, viewModel: QuestionViewModel) {
//    Row(
//        horizontalArrangement = Arrangement.SpaceBetween,
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp)
//    ) {
//        // 🔙 Кнопка "Назад"
//        IconButton(
//            onClick = {
//                if (viewModel.currentQuestionIndex > 0) {
//                    viewModel.saveCurrentQuestionState()
//                    viewModel.currentQuestionIndex--
//                }
//            },
//            enabled = viewModel.currentQuestionIndex > 0
//        ) {
//            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Previous")
//        }
//
//        // ✅ Если тест завершён → кнопка завершения
//        if (viewModel.allQuestionsAnswered()) {
//            IconButton(
//                onClick = {
//                    navController.navigate("result_screen/${viewModel.correctAnswersCount}")
//                }
//            ) {
//                Icon(imageVector = Icons.Filled.Check, contentDescription = "Finish")
//            }
//        } else {
//            // 🔜 Кнопка "Вперёд"
//            IconButton(
//                onClick = {
//                    viewModel.saveCurrentQuestionState()
//                    viewModel.moveToNextQuestion()
//                    navController.navigate("exam_screen/${viewModel.currentQuestionIndex}") {
//                        launchSingleTop = true
//                    }
//                }
//            ) {
//                Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = "Next")
//            }
//        }
//    }
//}
//
//
//
//// Функция для форматирования оставшегося времени (минуты:секунды)
//fun formatTime(millis: Long): String {
//    val minutes = (millis / 1000) / 60
//    val seconds = (millis / 1000) % 60
//    return String.format("%02d:%02d", minutes, seconds)
//}
