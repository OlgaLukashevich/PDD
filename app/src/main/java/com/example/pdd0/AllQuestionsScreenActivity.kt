package com.example.pdd0

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
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
                composable("question_screen/{ticketNumber}") { backStackEntry ->
                    val ticketNumber = backStackEntry.arguments?.getString("ticketNumber")?.toIntOrNull() ?: 1
                    QuestionScreen(navController, ticketNumber, questionViewModel) // ✅ Передаём ViewModel
                }



            }
        }
    }
}



@Composable
fun AllQuestionsScreen(navController: NavController, viewModel: QuestionViewModel, questionList: List<Question>) {
    val context = LocalContext.current
    var filteredTickets by remember { mutableStateOf(questionList.map { it.ticket_number }) } // ✅ Теперь сразу содержит все билеты

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
            Text(text = "Билеты", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Добавляем ПОИСК по билетам
        SearchBar(
            ticketList = questionList.map { it.ticket_number }.distinct(),
            onSearchResults = { filteredTickets = it } // ✅ Обновляем список билетов
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Отображаем найденные билеты
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredTickets) { ticketNumber ->
                TicketItem(ticketNumber, questionList, navController, viewModel)
            }
        }
    }
}

@Composable
fun TicketItem(ticketNumber: String, questionList: List<Question>, navController: NavController, viewModel: QuestionViewModel) {
    val favoriteTickets by viewModel.favoriteTickets.collectAsState() // ✅ Следим за избранными билетами
    val isFavorite = favoriteTickets.contains(ticketNumber) // ✅ Проверяем статус билета

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                val firstQuestionIndex = questionList.indexOfFirst { it.ticket_number == ticketNumber } // ✅ Ищем первый вопрос билета

                if (firstQuestionIndex != -1) {
                    Log.d("TicketItem", "Открываю билет: $ticketNumber, Первый вопрос: $firstQuestionIndex")
                    navController.navigate("question_screen/$firstQuestionIndex") // ✅ Передаём правильный индекс
                } else {
                    Log.e("TicketItem", "Ошибка: Вопросы для билета $ticketNumber не найдены")
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = ticketNumber,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f)
        )

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
                tint = if (isFavorite) Color.Yellow else Color.Gray // ✅ Цвет изменяется
            )
        }
    }
}


