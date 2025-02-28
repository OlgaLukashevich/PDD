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
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.example.pdd0.parser.parseJson

class QuestionScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Навигация
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "main_screen") {
                composable("main_screen") { MainScreen(navController) }
                composable("question_screen/{questionIndex}") { backStackEntry ->
                    // Извлекаем индекс вопроса из аргументов
                    val questionIndex = backStackEntry.arguments?.getString("questionIndex")?.toIntOrNull() ?: 1
                    QuestionScreen(navController = navController, questionIndex = questionIndex - 1) // Индекс начинается с 0
                }
                composable("all_questions_screen") {
                    AllQuestionsScreen(navController = navController)
                }
            }
        }
    }
}



@Composable
fun QuestionScreen(navController: NavController, questionIndex: Int) {
    var currentQuestionIndex by remember { mutableStateOf(questionIndex) } // Начинаем с переданного индекса
    var selectedAnswer by remember { mutableStateOf<String?>(null) } // Хранение выбранного ответа
    var isAnswerCorrect by remember { mutableStateOf(false) } // Проверка правильности ответа
    var questionStates by remember { mutableStateOf(mutableMapOf<Int, QuestionState>()) } // Состояние всех вопросов
    var isTestFinished by remember { mutableStateOf(false) } // Флаг завершения теста
    val questionList = parseJson(context = LocalContext.current) // Загрузка вопросов
    var isImageFullScreen by remember { mutableStateOf(false) } // Отслеживаем увеличение картинки

    // Переход от индекса к конкретному вопросу
    val currentQuestion = questionList.getOrNull(currentQuestionIndex)

    // Если данных нет, показываем заглушку
    if (currentQuestion == null) {
        Text(text = "Ошибка загрузки вопроса", fontSize = 24.sp)
        return
    }

    // Загружаем состояние для текущего вопроса (если оно есть)
    val currentState = questionStates[currentQuestionIndex]
    selectedAnswer = currentState?.selectedAnswer
    isAnswerCorrect = currentState?.isAnswerCorrect ?: false
    val isAnswerLocked = currentState?.isAnswerLocked ?: false

    // Подсчет правильных ответов
    val correctAnswersCount = questionStates.values.count { it.isAnswerCorrect }

    // Проверяем, завершил ли пользователь все вопросы
       if (isTestFinished) {
        // Показываем результат, когда тест завершен
        ResultScreen(correctAnswersCount = correctAnswersCount, totalQuestions = questionList.size, navController = navController)
        return
    }


    // Проверяем, если все вопросы отвечены, то показываем результат
    if (currentQuestionIndex == 10) { //== questionList.size
        // Завершаем тест
        isTestFinished = true
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
        QuestionNavigationPanel(navController, currentQuestionIndex)
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
            AnswerButton(
                answerText = answer.answer_text,
                isCorrect = answer.is_correct,
                isSelected = answer.answer_text == selectedAnswer,
                onClick = {
                    if (!isAnswerLocked) {
                        selectedAnswer = answer.answer_text
                        isAnswerCorrect = answer.is_correct
                        // Сохраняем ответ и блокируем возможность изменения
                        questionStates[currentQuestionIndex] = QuestionState(selectedAnswer, isAnswerCorrect, true)
                    }
                },
                isAnswerCorrect = isAnswerCorrect
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
            IconButton(onClick = {
                if (currentQuestionIndex > 0) {
                    currentQuestionIndex -= 1
                    selectedAnswer = questionStates[currentQuestionIndex]?.selectedAnswer
                    isAnswerCorrect = questionStates[currentQuestionIndex]?.isAnswerCorrect ?: false
                }
            },
                modifier = Modifier.size(48.dp)  // Увеличиваем размер кнопок
            ) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Previous")
            }
            // Кнопка "Вперед"
            IconButton(onClick = {
                if (currentQuestionIndex < questionList.size - 1) {
                    currentQuestionIndex += 1
                    selectedAnswer = questionStates[currentQuestionIndex]?.selectedAnswer
                    isAnswerCorrect = questionStates[currentQuestionIndex]?.isAnswerCorrect ?: false
                }
                else {
                    // Завершаем тест после последнего вопроса
                    isTestFinished = true
                }
            },
                modifier = Modifier.size(48.dp)  // Увеличиваем размер кнопок
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
fun QuestionNavigationPanel(navController: NavController, currentQuestionIndex: Int) {
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
                    .clickable { // Переход на экран с соответствующим вопросом
                        navController.navigate("question_screen/${index-1}") },
                color = if (index == currentQuestionIndex + 1) Color.Black else Color.Gray
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