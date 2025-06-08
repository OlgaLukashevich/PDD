package com.example.pdd0

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.pdd0.dataClass.Question
import com.example.pdd0.dataStore.FavoriteTicketsManager
import com.example.pdd0.parser.parseJson
import com.example.pdd0.utils.SearchBar


class AllQuestionsScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            // ✅ Создаём FavoriteTicketsManager
            val favoriteTicketsManager = FavoriteTicketsManager(applicationContext)
            val questionList = parseJson(LocalContext.current)


            // ✅ Создаём ViewModel через фабрику
            val questionViewModel: QuestionViewModel = viewModel(
                factory = QuestionViewModelFactory(favoriteTicketsManager)
            )
            NavHost(navController = navController, startDestination = "all_questions_screen") {
                composable("all_questions_screen") {
                    AllQuestionsScreen(navController, questionViewModel, questionList)
                }
                composable("question_screen/{ticketNumber}/{screenRoute}") { backStackEntry ->
                    val ticketNumber = backStackEntry.arguments?.getString("ticketNumber")?.toIntOrNull() ?: 1
                    val screenRoute = backStackEntry.arguments?.getString("screenRoute") ?: "question_screen" // Default to exam_screen
                    QuestionScreen(navController, ticketNumber, questionViewModel, screenRoute)
                }



            }
        }
    }
}



@Composable
fun AllQuestionsScreen(navController: NavController, viewModel: QuestionViewModel, questionList: List<Question>) {
    val context = LocalContext.current
    var filteredTickets by remember { mutableStateOf(questionList.map { it.ticket_number }) } // ✅ Теперь сразу содержит все билеты

    val ticketResults by viewModel.ticketResults.collectAsState()


    // Сортируем билеты по номеру (преобразуем их в числа)
    filteredTickets = filteredTickets
        .distinct()  // Убираем дубли
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
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
//            IconButton(onClick = { navController.popBackStack("main_screen", inclusive = false) }) {
//                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
//            }
                IconButton(onClick = {
                    navController.navigate("main_screen") {
                        popUpTo("main_screen") { inclusive = true } // Удаляем все экраны до "main_screen", включая его
                    }
                }) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                }


                Text(text = "Билеты", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF434348))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Добавляем ПОИСК по билетам
        SearchBar(
            ticketList = questionList.map { it.ticket_number }.distinct(),
            onSearchResults = { filteredTickets = it } // ✅ Обновляем список билетов
        )

        Spacer(modifier = Modifier.height(16.dp))
            // Текст "ПДД РБ" и "изучаем" в закрашенной рамке

                // ✅ Отображаем найденные билеты с увеличенным расстоянием и разделяющей полосой
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredTickets) { ticketNumber ->
//                        val correctAnswers = 9 // Здесь вычисляете количество правильных ответов для данного билета (например, из ViewModel)
                        TicketItem(
                            ticketNumber = ticketNumber,
                            questionList = questionList,
                            navController = navController,
                            viewModel = viewModel,
                            correctAnswers = ticketResults[ticketNumber] ?: 0
                        )



                        // Разделитель после каждого билета
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp), // Увеличиваем расстояние
                            color = Color.Gray, // Цвет разделителя
                            thickness = 1.dp // Толщина разделителя

                        )

                    }

            }
        }
    }
}

@Composable
fun TicketItem(
    ticketNumber: String,
    questionList: List<Question>,
    navController: NavController,
    viewModel: QuestionViewModel,
    correctAnswers: Int
) {
    val favoriteTickets by viewModel.favoriteTickets.collectAsState() // ✅ Следим за избранными билетами
    val isFavorite = favoriteTickets.contains(ticketNumber) // ✅ Проверяем статус билета
//    val resultText = "$correctAnswersCount/10"
    val correctFraction = correctAnswers / 10f
    var showTooltip by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }


    val progressColor =
        if (correctAnswers < 3) Color(0xFFD96B6B) else Color(0xFF4CAF50) // красный или зелёный

    // 🎯 Обрабатываем долгий клик
    val longPressModifier = Modifier.pointerInput(Unit) {
        detectTapGestures(
            onLongPress = {
                showDeleteDialog = true
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .then(longPressModifier) // 👈 добавляем модификатор

            .clickable {
                val firstQuestionIndex =
                    questionList.indexOfFirst { it.ticket_number == ticketNumber } // ✅ Ищем первый вопрос билета

                if (firstQuestionIndex != -1) {

                    navController.navigate("question_screen/$firstQuestionIndex/exam_screen") // Передаем индекс и режим
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
            modifier = Modifier.weight(1f) // Закрашенная рамка

        )
        // ✅ Прогресс-бар между номером билета и звездой
        Box(
            modifier = Modifier
                .width(150.dp) // увеличенная ширина
                .height(20.dp) // увеличенная высота
                .clip(RoundedCornerShape(6.dp))
                .background(Color.LightGray)
                .clickable { showTooltip = !showTooltip },
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(correctFraction.coerceIn(0f, 1f))
                    .background(progressColor)
            )
        }




        Spacer(modifier = Modifier.width(8.dp))
        // ⭐ Кнопка избранного

        // ✅ Кликабельная звезда для добавления/удаления из избранного
        IconButton(
            onClick = {
                Log.d("TicketItem", "Переключаю статус избранного для билета: $ticketNumber")
                viewModel.toggleFavoriteTicket(ticketNumber)
            }
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder, // ✅ Закрашенная или пустая звезда
                contentDescription = if (isFavorite) "Удалить из избранного" else "Добавить в избранное",
                tint = if (isFavorite) Color.Yellow else Color(0xFF434348) // ✅ Цвет изменяется
            )
        }
        // 💬 Подсказка (Tooltip)
        if (showTooltip) {
            DropdownMenu(
                expanded = true,
                onDismissRequest = { showTooltip = false }
            ) {
                Text(
                    text = "$correctAnswers из 10",
                    color = Color.DarkGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(8.dp)

                )
            }
        }
    }
        // 🧾 Диалог подтверждения удаления
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Удалить прогресс") },
                text = { Text("Вы уверены, что хотите удалить прогресс по билету $ticketNumber?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.removeTicketResult(ticketNumber)
                        showDeleteDialog = false
                    }) {
                        Text("Да")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Нет")
                    }
                }
            )
        }
    }





