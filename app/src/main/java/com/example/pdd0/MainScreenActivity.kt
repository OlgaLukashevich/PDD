package com.example.pdd0

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*

class MainScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Навигация
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "main_screen") {
                composable("main_screen") { MainScreen(navController) }
//                composable("question_screen") { QuestionScreen(navController) }
// Это правильный маршрут с передачей параметра индекса
                composable("question_screen/{questionIndex}") { backStackEntry ->
                    val questionIndex = backStackEntry.arguments?.getString("questionIndex")?.toIntOrNull() ?: 0
                    QuestionScreen(navController = navController, questionIndex = questionIndex)
                }
                composable("favorite_question_screen") { FavoriteQuestionScreen(navController = navController) }  // Новый экран

                composable("all_questions_screen") {
                    AllQuestionsScreen(navController = navController)
                }


            }
        }
    }
}

@Composable
fun MainScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Поиск
        SearchBar()

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
        MenuButtons(navController)

        Spacer(modifier = Modifier.height(32.dp))

        // Иконки социальных сетей
        SocialIcons()
    }
}

@Composable
fun MenuButtons(navController: NavController) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MenuButton("Случайный билет", navController)
        MenuButton("Все билеты", navController)
        MenuButton("Избранные билеты", navController)
        MenuButton("Экзамен", navController)
    }
}



@Composable
fun MenuButton(text: String, navController: NavController) {
    Button(
        onClick = {
            when (text) {
                "Случайный билет" -> navController.navigate("question_screen/0")
                "Все билеты" -> navController.navigate("all_questions_screen")
                "Избранные билеты" -> navController.navigate("favorite_question_screen") // Переход на избранные билеты
                "Экзамен" -> navController.navigate("exam_screen") // Пример с экраном экзамена
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


@Composable
fun SearchBar() {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Поиск билета") },
            modifier = Modifier.weight(1f),
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
            }
        )
    }
}

@Composable
fun SocialIcons() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_camera),
            contentDescription = "Icon 1"
        )
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_camera),
            contentDescription = "Icon 2"
        )
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_camera),
            contentDescription = "Icon 3"
        )
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_camera),
            contentDescription = "Icon 4"
        )
    }
}