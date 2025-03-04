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
import com.example.pdd0.parser.parseJson

class AllQuestionsScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val questionViewModel: QuestionViewModel = viewModel() // ✅ Создаём ViewModel

            NavHost(navController = navController, startDestination = "all_questions_screen") {
                composable("all_questions_screen") {
                    AllQuestionsScreen(navController, questionViewModel)
                }
                composable("question_screen/{ticketNumber}") { backStackEntry ->
                    val ticketNumber = backStackEntry.arguments?.getString("ticketNumber")?.toIntOrNull() ?: 1
                    QuestionScreen(navController, ticketNumber) // ✅ Передаём как Int
                }



            }
        }
    }
}



@Composable
fun AllQuestionsScreen(navController: NavController, viewModel: QuestionViewModel) {
    var ticketList by remember { mutableStateOf<List<Question>>(emptyList()) }
    val context = LocalContext.current

    // ✅ Загружаем вопросы один раз
    LaunchedEffect(Unit) {
        ticketList = parseJson(context)
    }

    val uniqueTickets = ticketList
        .groupBy { it.ticket_number }
        .keys.toList()
        .sortedBy { it.substringAfter("Билет ").toIntOrNull() ?: Int.MAX_VALUE }

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

        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(uniqueTickets) { ticketNumber ->
                TicketItem(ticketNumber, ticketList, navController, viewModel) // ✅ Передаём список вопросов
            }
        }
    }
}



@Composable
fun TicketItem(ticketNumber: String, questionList: List<Question>, navController: NavController, viewModel: QuestionViewModel) {
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
        Icon(
            imageVector = Icons.Filled.StarBorder,
            contentDescription = "Favorite",
            tint = Color.Gray
        )
    }
}

