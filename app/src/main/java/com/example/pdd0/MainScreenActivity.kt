package com.example.pdd0

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.pdd0.dataClass.Question
import com.example.pdd0.dataStore.FavoriteTicketsManager
import com.example.pdd0.parser.parseJson
import com.example.pdd0.utils.SocialIcons


class MainScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Навигация

            val navController = rememberNavController()


            // ✅ Создаём FavoriteTicketsManager перед использованием ViewModel
            val favoriteTicketsManager = FavoriteTicketsManager(applicationContext)

            // ✅ Используем фабрику, чтобы ViewModel получил FavoriteTicketsManager
            val questionViewModel: QuestionViewModel = viewModel(
                factory = QuestionViewModelFactory(favoriteTicketsManager)
            )
            val questionList = parseJson(LocalContext.current)


            NavHost(navController = navController, startDestination = "main_screen") {
                composable("main_screen") {
                    MainScreen(navController, questionViewModel, questionList)
                }
//                composable("question_screen") { QuestionScreen(navController) }
// Это правильный маршрут с передачей параметра индекса
                composable("question_screen/{questionIndex}") { backStackEntry ->
                    val questionIndex = backStackEntry.arguments?.getString("questionIndex")?.toIntOrNull() ?: 0
                    QuestionScreen(navController = navController, questionIndex, questionViewModel)
                }
                composable("favorite_question_screen") {
                    FavoriteQuestionScreen(
                        navController = navController,
                        viewModel = questionViewModel // ✅ Передаём правильное имя ViewModel
                    )
                }

                composable("all_questions_screen") {
                    AllQuestionsScreen(navController = navController, questionViewModel, questionList) // ✅ Передаём viewModel
                }

                composable("exam_screen/{questionIndex}") { backStackEntry ->
                    val questionIndex = backStackEntry.arguments?.getString("questionIndex")?.toIntOrNull() ?: 0
                    ExamScreen(navController = navController, questionIndex, questionViewModel)
                }


                // ✅ Поддерживаем передачу `correctAnswersCount`
                composable("result_screen/{correctAnswers}") { backStackEntry ->
                    val correctAnswers = backStackEntry.arguments?.getString("correctAnswers")?.toIntOrNull() ?: 0
                    ResultScreen(correctAnswers, 10, navController, questionViewModel)
                }


            }
        }
    }
}



@Composable
fun MainScreen(navController: NavController, questionViewModel: QuestionViewModel, questionList: List<Question>) {
   // var filteredTickets by remember { mutableStateOf<List<String>>(questionList.map { it.ticket_number }) } // ✅ Теперь изначально содержит все билеты

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
         Spacer(modifier = Modifier.height(32.dp))

        // Заголовок
        Text(
            text = "ПДД РБ",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "изучаем",
            fontSize = 18.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Кнопки меню
        MenuButtons(navController, questionViewModel)

        Spacer(modifier = Modifier.height(32.dp))

        // Иконки социальных сетей
        SocialIcons()
    }
}

@Composable
fun MenuButtons(navController: NavController, viewModel: QuestionViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MenuButton("Случайный билет", navController, viewModel)
        MenuButton("Все билеты", navController, viewModel)
        MenuButton("Избранные билеты", navController, viewModel)
        MenuButton("Экзамен", navController, viewModel)
    }
}

@Composable
fun MenuButton(text: String, navController: NavController, viewModel: QuestionViewModel) {
    Button(
        onClick = {
            when (text) {
                "Случайный билет" -> {
                    val randomTicket = (0 until 40).random() * 10 // ✅ Выбираем случайный билет
                    viewModel.currentTicketStartIndex = randomTicket // ✅ Запоминаем его стартовый индекс
                    viewModel.currentQuestionIndex = randomTicket // ✅ Ставим первый вопрос случайного билета
                    navController.navigate("question_screen/$randomTicket")
                }


                "Все билеты" -> {
                    navController.navigate("all_questions_screen") // ✅ Если в NavHost передан viewModel, всё будет работать
                }                "Избранные билеты" -> navController.navigate("favorite_question_screen")
                "Экзамен" -> {
                    viewModel.loadRandomTicket() // ✅ Загружаем случайный билет
                    val startIndex = viewModel.currentQuestionIndex // ✅ Берём индекс первого вопроса билета
                    navController.navigate("exam_screen/$startIndex") // ✅ Передаём индекс в навигацию
                }

            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}




