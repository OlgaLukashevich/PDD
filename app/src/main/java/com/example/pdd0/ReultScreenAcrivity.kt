package com.example.pdd0

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@Composable
fun ResultScreen(correctAnswersCount: Int, totalQuestions: Int, navController: NavController, viewModel: QuestionViewModel) {
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

        // 🔥 Кнопка "Пройти заново"
        Button(onClick = {
            viewModel.resetTest() // ✅ Сбрасываем тест перед навигацией
            val restartIndex = viewModel.currentTicketStartIndex // ✅ Используем запомненный билет
            navController.navigate("question_screen/$restartIndex") {
                popUpTo("main_screen") { inclusive = false } // ✅ Удаляем старые экраны
            }
        }) {
            Text("Пройти заново")
        }



        Spacer(modifier = Modifier.height(16.dp))
        // Кнопка добавить в избранное
        Button(onClick = {navController.navigate("favorite_question_screen")}) {
            Text("Добавить в избранное")
        }
        Spacer(modifier = Modifier.height(16.dp))
        // 🔥 Кнопка "Следующий билет"
        Button(onClick = {
            viewModel.loadRandomTicket()
            navController.navigate("question_screen/${viewModel.currentQuestionIndex}") {
                popUpTo("main_screen") // Удаляем предыдущие экраны
            }
        }) {
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
