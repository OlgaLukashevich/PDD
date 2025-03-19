package com.example.pdd0

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pdd0.parser.parseJson
import androidx.compose.ui.unit.dp



@Composable
fun ResultScreen(correctAnswersCount: Int, totalQuestions: Int, navController: NavController, viewModel: QuestionViewModel) {
    val resultText = "$correctAnswersCount/10"
    val context = LocalContext.current
    val questionList = parseJson(context) // ✅ Загружаем список всех вопросов
    val currentTicketNumber = viewModel.getCurrentTicketNumber(questionList) // ✅ Определяем номер билета

    val favoriteTickets by viewModel.favoriteTickets.collectAsState() // ✅ Следим за избранными билетами
    val isFavorite = favoriteTickets.contains(currentTicketNumber) // ✅ Проверяем, в избранном ли билет

    val ticketProgress = viewModel.getTicketProgress("1") // Получаем прогресс для билета 1 (или другого билета)

    // Определяем сообщение в зависимости от количества правильных ответов
    val resultMessage = when {
        correctAnswersCount == 10 -> "Шикарно! Вы правильно ответили на все вопросы!"
        correctAnswersCount >= 7 -> "Хороший результат! Но есть несколько ошибок. Попробуйте ещё раз!"
        else -> "Не расстраивайтесь, вы обязательно сможете улучшить результат. Пройдите тест ещё раз."
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Фоновое изображение
        Image(
            painter = painterResource(id = R.drawable.result_background), // Замените на ваш ресурс изображения
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
            Text(
                text = "Тест завершен!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White, // Белый цвет для контраста
                style = TextStyle(
                    letterSpacing = 1.5.sp, // Текст с небольшим расстоянием между буквами
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ваш результат: $resultText",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = if (correctAnswersCount == 10) Color.Green else Color.Red, //== totalQuestions
                style = TextStyle(
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center,
                    background = Color(0xFF0D6B5E) // Легкий белый фон для текста
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = resultMessage,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                style = TextStyle(
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center,
                    background = Color(0x88000000) // Полупрозрачный черный фон для контраста
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 🔥 Кнопка "Пройти заново"
            Button(
                onClick = {
                    viewModel.resetTest() // ✅ Сбрасываем тест перед навигацией
                    val restartIndex =
                        viewModel.currentTicketStartIndex // ✅ Используем запомненный билет
                    navController.navigate("question_screen/$restartIndex") {
                        popUpTo("main_screen") { inclusive = false } // ✅ Удаляем старые экраны
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6AC06E), // Зеленый
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp) // Скругленные углы
            ) {
                Text("Пройти заново", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🔥 Кнопка "Следующий билет"
            Button(
                onClick = {
                    viewModel.loadRandomTicket()
                    navController.navigate("question_screen/${viewModel.currentQuestionIndex}") {
                        popUpTo("main_screen") // Удаляем предыдущие экраны
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9BACB0), // Серый
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp) // Скругленные углы
            ) {
                Text("Следующий билет", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка "Главная"
            Button(
                onClick = {
                    viewModel.resetTest() // ✅ Добавляем сброс состояния теста
                    navController.navigate("main_screen") {
                        popUpTo("main_screen") { inclusive = true } // ✅ Удаляем все предыдущие экраны
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7EA6B9), // Более темный серый
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp) // Скругленные углы
            ) {
                Text("Главная", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

        }
    }
}
