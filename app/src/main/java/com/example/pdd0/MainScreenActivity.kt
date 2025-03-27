package com.example.pdd0

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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

                composable("question_screen/{questionIndex}/{screenRoute}") { backStackEntry ->
                    val questionIndex = backStackEntry.arguments?.getString("questionIndex")?.toIntOrNull() ?: 0
                    val screenRoute = backStackEntry.arguments?.getString("screenRoute") ?: "question_screen"
                    QuestionScreen(navController = navController, questionIndex, questionViewModel, screenRoute)
                }

                composable("favorite_question_screen") {
                    FavoriteQuestionScreen(
                        navController = navController,
                        viewModel = questionViewModel //  Передаём имя ViewModel
                    )
                }

                composable("all_questions_screen") {
                    AllQuestionsScreen(navController = navController, questionViewModel, questionList) // ✅ Передаём viewModel
                }

                composable("exam_screen/{questionIndex}/{screenRoute}") { backStackEntry ->
                    val questionIndex = backStackEntry.arguments?.getString("questionIndex")?.toIntOrNull() ?: 0
                    val screenRoute = backStackEntry.arguments?.getString("screenRoute") ?: "exam_screen"
                    ExamScreen(navController, questionIndex, questionViewModel, screenRoute)
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Текст "ПДД РБ" и "изучаем" в закрашенной рамке
            Box(
                modifier = Modifier
                    .fillMaxWidth() // Полоска на всю ширину экрана
                    .background(Color(0xFF34bfff).copy(alpha = 0.8f), RoundedCornerShape(8.dp)) // Закрашенная рамка
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp), // Отступы по бокам
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ПДД",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black // Серый цвет текста
                    )
                    Text(
                        text = "изучаем",
                        fontSize = 18.sp,
                        color = Color.Gray // Серый цвет текста
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Растягиваем пространство между текстом и кнопками

            // Кнопки меню по центру экрана
            MenuButtons(navController, questionViewModel)

            Spacer(modifier = Modifier.weight(1f)) // Растягиваем пространство между кнопками и иконками
        }

        // Иконки социальных сетей справа
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            SocialIcons()
        }
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
        var isPressed by remember { mutableStateOf(false) } // Состояние кнопки

        Button(
            onClick = {
                when (text) {
                    "Случайный билет" -> {
                        val randomTicket = (0 until 40).random() * 10 // Выбираем случайный билет
                        viewModel.currentTicketStartIndex =
                            randomTicket // Запоминаем его стартовый индекс
                        viewModel.currentQuestionIndex =
                            randomTicket // Ставим первый вопрос случайного билета
                        navController.navigate("question_screen/$randomTicket/question_screen") //
                    }
                    "Все билеты" -> {
                        navController.navigate("all_questions_screen") // ✅ Если в NavHost передан viewModel, всё будет работать
                    }
                    "Избранные билеты" -> navController.navigate("favorite_question_screen")
                    "Экзамен" -> {
                        viewModel.loadRandomTicket() // ✅ Загружаем случайный билет
                        val startIndex =
                            viewModel.currentQuestionIndex // ✅ Берём индекс первого вопроса билета
                        navController.navigate("exam_screen/$startIndex/exam_screen") // ✅ Передаём индекс в навигацию
                    }
                }
                isPressed = !isPressed // Изменяем состояние при нажатии
            },
            modifier = Modifier
                .width(200.dp) // Ширина кнопки
                .height(48.dp), // Высота кнопки
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPressed)    Color.Gray.copy(alpha = 0.9f)  else Color(
                    0xFF16B4AB
                ) // Серозеленый цвет  Color(0xFFB0EAC8)
            )
        ) {
            Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }





