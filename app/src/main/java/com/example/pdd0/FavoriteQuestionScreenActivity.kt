package com.example.pdd0

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.pdd0.dataClass.Question
import com.example.pdd0.parser.parseJson

@Composable
fun FavoriteQuestionScreen(navController: NavController, viewModel: QuestionViewModel) {
    var questionList by remember { mutableStateOf<List<Question>>(emptyList()) }
    val favoriteTickets by viewModel.favoriteTickets.collectAsState()
    val context = LocalContext.current

    // ✅ Загружаем список вопросов, когда обновляются избранные билеты
    LaunchedEffect(favoriteTickets) {
        questionList = parseJson(context)
    }

    // ✅ Логируем данные для отладки
    Log.d("FavoriteScreen", "Избранные билеты (ticket_number): $favoriteTickets")
    Log.d("FavoriteScreen", "Всего вопросов: ${questionList.size}")

    // ✅ Улучшенная фильтрация избранных билетов
    val favoriteQuestionList = questionList.filter { question ->
        val cleanTicketNumber = question.ticket_number.replace("Билет ", "").trim()

        // ✅ Проверяем все возможные форматы: "Билет N", "N"
        favoriteTickets.any { favTicket ->
            favTicket == cleanTicketNumber || favTicket == "Билет $cleanTicketNumber"
        }
    }

    // ✅ Проверяем, сколько вопросов осталось после фильтрации
    Log.d("FavoriteScreen", "После фильтрации осталось вопросов: ${favoriteQuestionList.size}")

    val uniqueFavoriteTickets = favoriteQuestionList
        .map { it.ticket_number }
        .distinct()
        .sortedBy { it.toIntOrNull() ?: Int.MAX_VALUE }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack("main_screen", inclusive = false) }) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(text = "Избранные билеты", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (uniqueFavoriteTickets.isEmpty()) {
            Log.d("FavoriteScreen", "❌ Избранных билетов нет, показываем сообщение")
            Text(
                text = "Избранных билетов пока нет.",
                fontSize = 18.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            Log.d("FavoriteScreen", "✅ Отображаем список избранных билетов: $uniqueFavoriteTickets")
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uniqueFavoriteTickets) { ticketNumber ->
                    FavoriteTicketItem(ticketNumber, favoriteQuestionList, navController, viewModel)
                }
            }
        }
    }
}


@Composable
fun FavoriteTicketItem(ticketNumber: String, questionList: List<Question>, navController: NavController, viewModel: QuestionViewModel) {
    val context = LocalContext.current
    val allQuestions = parseJson(context) // Загружаем ВСЕ вопросы

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable {
                // ✅ Ищем первый вопрос билета в общем списке
                val firstQuestionIndex = allQuestions.indexOfFirst { it.ticket_number == ticketNumber }

                if (firstQuestionIndex != -1) {
                    Log.d("FavoriteTicketItem", "Открываю избранный билет: $ticketNumber, Первый вопрос: $firstQuestionIndex")
                    navController.navigate("question_screen/$firstQuestionIndex") // ✅ Передаём правильный индекс
                } else {
                    Log.e("FavoriteTicketItem", "Ошибка: Вопросы для билета $ticketNumber не найдены")
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = ticketNumber,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "Favorite",
            tint = Color.Yellow
        )
    }
}


