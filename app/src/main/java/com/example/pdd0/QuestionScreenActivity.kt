package com.example.pdd0

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pdd0.parser.parseJson


class QuestionScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Навигация
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "main_screen") {
                composable("main_screen") { MainScreen(navController) }
                composable("question_screen") { QuestionScreen(navController) }
                composable("all_questions_screen") {
                    AllQuestionsScreen(navController = navController)
                }
            }
        }
    }
}


@Composable
fun QuestionScreen(navController: NavController) {
    var currentQuestionIndex by remember { mutableStateOf(0) } // Начальный индекс вопроса
    var selectedAnswer by remember { mutableStateOf<String?>(null) } // Хранение выбранного ответа
    var isAnswerCorrect by remember { mutableStateOf(false) } // Проверка, правильный ли ответ
    val questionList = parseJson(context = LocalContext.current) // Загрузка вопросов

    // Переход от индекса к конкретному вопросу
    val currentQuestion = questionList.getOrNull(currentQuestionIndex)

    // Если данных нет, показываем заглушку
    if (currentQuestion == null) {
        Text(text = "Ошибка загрузки вопроса", fontSize = 24.sp)
        return
    }
    var showPauseDialog by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Панель навигации
        QuestionNavigationPanel(navController, currentQuestionIndex)

        Spacer(modifier = Modifier.height(16.dp))

        // Заголовок с номером билета
        Text(
            text = "${currentQuestion.ticket_number}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Вопрос
        Text(
            text = currentQuestion.question,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Ответы
        currentQuestion.answers.forEach { answer ->
            AnswerButton(
                answerText = answer.answer_text,
                isCorrect = answer.is_correct,
                isSelected = answer.answer_text == selectedAnswer,
                onClick = {
                    selectedAnswer = answer.answer_text
                    isAnswerCorrect = answer.is_correct
                },
                isAnswerCorrect = isAnswerCorrect
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Навигационные кнопки
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = {
                if (currentQuestionIndex > 0) {
                    currentQuestionIndex -= 1
                    selectedAnswer = null // Сбросить выбранный ответ
                    isAnswerCorrect = false // Сбросить статус ответа
                }
            }) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Previous")
            }
            IconButton(onClick = {
                if (currentQuestionIndex < questionList.size - 1) {
                    currentQuestionIndex += 1
                    selectedAnswer = null // Сбросить выбранный ответ
                    isAnswerCorrect = false // Сбросить статус ответа
                }
            }) {
                Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = "Next")
            }

        }
    }

}





@Composable
fun QuestionNavigationPanel(navController: NavController, currentQuestionIndex: Int) {
    var showPauseDialog by remember { mutableStateOf(false) }
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Кнопка паузы
        IconButton(onClick = { showPauseDialog = true }) {
            Icon(imageVector = Icons.Filled.Pause, contentDescription = "Pause")
        }
        // Навигация по вопросам
        (1..10).forEach { index ->  // Можно заменить диапазон, чтобы он был от 1 до количества вопросов
            Text(
                text = "$index",
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(4.dp)
                    .clickable { /* Handle question navigation */ },
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
            },
            onGoHome = {
                showPauseDialog = false
                navController.navigate("main_screen") // Переход на главный экран
            },
            onAddToFavorites = {
                // Логика для добавления в избранное
                showPauseDialog = false
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
