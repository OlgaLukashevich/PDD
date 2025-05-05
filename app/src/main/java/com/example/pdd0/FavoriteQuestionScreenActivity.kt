package com.example.pdd0

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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

       // ✅ Улучшенная фильтрация избранных билетов
    val favoriteQuestionList = questionList.filter { question ->
        val cleanTicketNumber = question.ticket_number.replace("Билет ", "").trim()

        // ✅ Проверяем все возможные форматы: "Билет N", "N"
        favoriteTickets.any { favTicket ->
            favTicket == cleanTicketNumber || favTicket == "Билет $cleanTicketNumber"
        }
    }

       val uniqueFavoriteTickets = favoriteQuestionList
        .map { it.ticket_number }
        .distinct()
           .sortedBy {
               it.replace(Regex("[^0-9]"), "").toIntOrNull() ?: Int.MAX_VALUE
           }  // Сортировка по числовому значению, извлеченному из строки



    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Фоновое изображение
        Image(
            painter = painterResource(id = R.drawable.main_background), // Замените на ваш ресурс изображения
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Масштабирование изображения
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    navController.popBackStack(
                        "main_screen",
                        inclusive = false
                    )
                }) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(text = "Избранные билеты", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF434348))
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
                Log.d(
                    "FavoriteScreen",
                    "✅ Отображаем список избранных билетов: $uniqueFavoriteTickets"
                )
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uniqueFavoriteTickets) { ticketNumber ->
                        FavoriteTicketItem(
                            ticketNumber,
                            favoriteQuestionList,
                            navController,
                            viewModel
                        )
                        // Добавляем разделитель после каждого билета
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),  // Отступы вокруг разделителя
                            thickness = 1.dp, // Толщина разделителя
                            color = Color.Gray // Цвет разделителя
                        )
                    }

                }
            }
        }
    }
}


@Composable
fun FavoriteTicketItem(ticketNumber: String, questionList: List<Question>, navController: NavController, viewModel: QuestionViewModel) {
    val context = LocalContext.current
    val allQuestions = parseJson(context) // Загружаем ВСЕ вопросы


    val favoriteTickets by viewModel.favoriteTickets.collectAsState()

    // Определяем, является ли текущий билет избранным
    val isFavorite = favoriteTickets.contains(ticketNumber)

    // Состояние для управления звездой
    var isStarFilled by remember { mutableStateOf(isFavorite) }




    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp)
            .clickable {
                // ✅ Ищем первый вопрос билета в общем списке
                val firstQuestionIndex = allQuestions.indexOfFirst { it.ticket_number == ticketNumber }

                if (firstQuestionIndex != -1) {
                    // Передаем правильный индекс и режим
                    navController.navigate("question_screen/$firstQuestionIndex/exam_screen") // Передаем индекс и режим
                } else {
                    Log.e("FavoriteTicketItem", "Ошибка: Вопросы для билета $ticketNumber не найдены")
                }
            }
            .clip(RoundedCornerShape(16.dp)) // Закругленные углы
            .background(Color(0xFFA9D6DE).copy(alpha = 0.5f)) // Добавляем полупрозрачный фон
            .padding(8.dp), // Отступы внутри
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = ticketNumber,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold, // Пожирненный текст
            color = Color(0xFF434348), // Цвет текста белый для лучшего контраста
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = {
                // Переключаем состояние звезды
                isStarFilled = !isStarFilled

                // Добавляем или удаляем билет из избранного
                if (isStarFilled) {
                    viewModel.addFavoriteTicket(ticketNumber) // Добавляем в избранное
                } else {
                    viewModel.removeFavoriteTicket(ticketNumber) // Удаляем из избранного
                }
            }
        ) {
            Icon(
                imageVector = if (isStarFilled) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = if (isStarFilled) "Удалить из избранного" else "Добавить в избранное",
                tint = if (isStarFilled) Color.Yellow else Color.Gray
            )
        }
    }
}


