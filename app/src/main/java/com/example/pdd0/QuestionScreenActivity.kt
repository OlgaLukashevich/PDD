package com.example.pdd0

import android.os.Bundle
import android.util.Log
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.pdd0.parser.parseJson

class QuestionScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Навигация
            val navController = rememberNavController()
            val questionViewModel: QuestionViewModel = viewModel() // Создаём ViewModel

            NavHost(navController = navController, startDestination = "main_screen") {
                composable("main_screen") { MainScreen(navController) }
                composable("question_screen/{questionIndex}") { backStackEntry ->
                // Извлекаем индекс вопроса из аргументов
                    val questionIndex = backStackEntry.arguments?.getString("questionIndex")?.toIntOrNull() ?: 1
                    QuestionScreen(navController, questionIndex - 1, questionViewModel) // Передаем ViewModel
                }
                composable("all_questions_screen") { AllQuestionsScreen(navController) }
            }
        }
    }
}

@Composable
fun QuestionScreen(navController: NavController, questionIndex: Int, viewModel: QuestionViewModel = viewModel()) {

     val questionList = parseJson(context = LocalContext.current) // Загрузка вопросов

    // Следим за currentQuestionIndex в ViewModel
  //  val currentQuestionIndex by remember { derivedStateOf { viewModel.currentQuestionIndex } }
//    // Загружаем сохранённые ответы перед рендерингом UI
//    LaunchedEffect(currentQuestionIndex) {
//        viewModel.loadQuestionState()
//    }
// Загружаем состояние для текущего вопроса (если оно есть)
//    val currentState by remember { derivedStateOf { viewModel.getCurrentQuestionState() } }
//    val selectedAnswer = viewModel.selectedAnswer
//    val isAnswerCorrect = viewModel.isAnswerCorrect
//    val isAnswerLocked = viewModel.getCurrentQuestionState().isAnswerLocked


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

        // Подсчет правильных ответов
    val correctAnswersCount = viewModel.questionStates.values.count { questionState: QuestionState -> questionState.isAnswerCorrect }

    // Проверяем, завершил ли пользователь все вопросы
    if (viewModel.isTestFinished) {
        ResultScreen(correctAnswersCount, questionList.size, navController)
        return
    }


    // Проверяем, если все вопросы отвечены, то показываем результат
    if (viewModel.currentQuestionIndex == 10) { //== questionList.size
        // Завершаем тест
        viewModel.isTestFinished = true
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

            // Выводим изображение вопроса
            if (!currentQuestion.image.isNullOrEmpty()) {  // Загружаем только если есть путь
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






        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)  // Добавляем отступы
                .align(Alignment.CenterHorizontally)  // Центрируем кнопки
                .zIndex(1f)  // Устанавливаем приоритет отображения
        ) {
            // Кнопка "Назад"
            IconButton(
                onClick = {
                    if (viewModel.currentQuestionIndex > 0) {
                        viewModel.saveCurrentQuestionState()
                        viewModel.currentQuestionIndex--
                    }
                },
                enabled = viewModel.currentQuestionIndex > 0 // Блокируем кнопку, если вопрос первый
            ) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Previous")
            }

            // Кнопка "Вперед"
            IconButton(
                onClick = {
                    if (viewModel.currentQuestionIndex < questionList.size - 1) {
                        viewModel.saveCurrentQuestionState()
                        viewModel.currentQuestionIndex++
                    }
                },
                enabled = viewModel.currentQuestionIndex < questionList.size - 1 // Блокируем, если последний вопрос
            ) {
                Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = "Next")
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
        // Навигация по вопросам
        (1..10).forEach { index ->  // Можно заменить диапазон, чтобы он был от 1 до количества вопросов
            Text(
                text = "$index",
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(4.dp)
                    .clickable {
                        // Сначала сохраняем ответ перед сменой вопроса
                        viewModel.saveCurrentQuestionState()

                        // Если индекс уже тот же самый, просто загружаем состояние
                        if (viewModel.currentQuestionIndex != index - 1) {
                            viewModel.currentQuestionIndex = index - 1
                            viewModel.loadQuestionState()
                        }

                        // Переход без пересоздания `QuestionScreen`
                        navController.navigate("question_screen/${index - 1}") {
                            launchSingleTop = true // Гарантируем, что не создаётся новый экран
                        }
                    },
                color = if (index == viewModel.currentQuestionIndex + 1) Color.Black else Color.Gray
            )
        }
    }


    // Диалог с вариантами действий
    if (showPauseDialog) {

        PauseDialog(
            navController = navController, // Передаем navController
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
                // Логика для добавления в избранное
                showPauseDialog = false
                isPaused = false // Возвращаем плей при добавлении в избранное
            }
        )
    }
}



@Composable
fun ResultScreen(correctAnswersCount: Int, totalQuestions: Int, navController: NavController) {
    val resultText = "$correctAnswersCount/10"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Тест завершен!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ваш результат: $resultText",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = if (correctAnswersCount == 10) Color.Green else Color.Red //== totalQuestions
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            // Логика для повторного прохождения теста или перехода к следующему билету
        }) {
            Text("Пройти заново")
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Кнопка добавить в избранное
        Button(onClick = {navController.navigate("favorite_question_screen")}) {
            Text("Добавить в избранное")
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Кнопка следующий билет
        Button(onClick = { }) {
            Text("Следующий билет")
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Кнопка вернуться на главную
        Button(onClick = {
            navController.navigate("main_screen") }) {
            Text("Главная")
        }
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
    onResume: () -> Unit,
    onGoHome: () -> Unit,
    onAddToFavorites: () -> Unit
) {
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
                TextButton(onClick = onAddToFavorites) {
                    Text("Добавить в избранное")
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