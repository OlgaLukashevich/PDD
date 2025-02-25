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
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.pdd0.dataClass.Question
import com.example.pdd0.parser.parseJson

class AllQuestionsScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Навигация
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "all_questions_screen") {
                composable("all_questions_screen") { AllQuestionsScreen(navController = navController) }
                composable("question_screen/{ticketId}") { backStackEntry ->
                    QuestionScreen(ticketId = backStackEntry.arguments?.getString("ticketId") ?: "")
                }
            }
        }
    }
}

@Composable
fun AllQuestionsScreen(navController: NavController) {
    // Состояние для хранения списка вопросов
    var ticketList by remember { mutableStateOf<List<Question>>(emptyList()) }
    val context = LocalContext.current

    // Загружаем данные
    LaunchedEffect(Unit) {
        try {
            ticketList = parseJson(context) // Загружаем вопросы из JSON
            Log.d("AllQuestionsScreen", "Загружено ${ticketList.size} вопросов.") // Логируем размер списка
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
        }
    }

    // Группируем вопросы по номерам билетов, чтобы отобразить только уникальные билеты
    val uniqueTickets = ticketList
        .groupBy { it.ticket_number }  // Группируем по номеру билета
        .keys.toList()  // Получаем только уникальные номера билетов
        .sortedBy { it.substringAfter("Билет ").toIntOrNull() ?: Int.MAX_VALUE } // Сортировка по числовому значению




    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Заголовок с кнопкой "Назад"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* Handle back button */ }) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Билеты",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Список билетов
        TicketList(uniqueTickets, navController)
    }
}

@Composable
fun TicketList(ticketNumbers: List<String>, navController: NavController) {
    if (ticketNumbers.isEmpty()) {
        Text(text = "Загружаются билеты...", fontSize = 18.sp)
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(ticketNumbers) { ticketNumber ->
            TicketItem(ticketNumber, navController)
        }
    }
}

@Composable
fun TicketItem(ticketNumber: String, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                // При клике на билет, переходим на экран с вопросами
                navController.navigate("question_screen/$ticketNumber")
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Название билета
        Text(
            text = "$ticketNumber",
            fontSize = 18.sp,
            modifier = Modifier.weight(1f)
        )

        // Иконка звезды
        Icon(
            imageVector = Icons.Filled.StarBorder,
            contentDescription = "Favorite",
            tint = Color.Gray
        )
    }
}


@Composable
fun QuestionScreen(ticketId: String) {
    val currentQuestionIndex = 1

    // Примерные данные. В реальном приложении можно подгружать данные по ticketId
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$ticketId",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Вопрос",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        AllQuestionsAnswerButton("Ответ 1")
        AllQuestionsAnswerButton("Ответ 2")
        AllQuestionsAnswerButton("Ответ 3")
        AllQuestionsAnswerButton("Ответ 4")

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { /* Handle previous question */ }) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Previous")
            }
            IconButton(onClick = { /* Handle next question */ }) {
                Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = "Next")
            }
        }
    }
}

@Composable
fun AllQuestionsAnswerButton(text: String) {
    Button(
        onClick = { /* Handle answer selection */ },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAllQuestionsScreen() {
    val navController = rememberNavController()
    AllQuestionsScreen(navController = navController)
}
