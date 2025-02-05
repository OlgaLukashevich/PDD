//package com.example.pdd0
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.ArrowForward
//import androidx.compose.material.icons.filled.PlayArrow
//import androidx.compose.material.icons.filled.StarBorder
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.text.font.FontWeight
//import androidx.navigation.NavController
//import androidx.navigation.compose.*
//
//class AllQuestionsScreenActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            // Навигация
//            val navController = rememberNavController()
//            NavHost(navController = navController, startDestination = "all_questions_screen") {
//                composable("all_questions_screen") { AllQuestionsScreen(navController = navController) }
//                composable("question_screen") { QuestionScreen() }
//            }
//        }
//    }
//}
//
//@Composable
//fun AllQuestionsScreen(navController: NavController) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        // Заголовок с кнопкой "Назад"
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            IconButton(onClick = { /* Handle back button */ }) {
//                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
//            }
//            Text(
//                text = "Билеты",
//                fontSize = 24.sp,
//                fontWeight = FontWeight.Bold
//            )
//        }
//
//        Spacer(modifier = Modifier.height(32.dp))
//
//        // Список билетов
//        QuestionList(navController)
//    }
//}
//
//@Composable
//fun QuestionList(navController: NavController) {
//    val questionList = List(7) { "Билет ${it + 1}" }
//
//    Column {
//        questionList.forEach { questionName ->
//            QuestionItem(questionName, navController)
//        }
//    }
//}
//
//@Composable
//fun QuestionItem(questionName: String, navController: NavController) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp)
//            .clickable {
//                // При клике на билет, переходим на экран с вопросами
//                navController.navigate("question_screen")
//            },
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        // Название билета
//        Text(
//            text = questionName,
//            fontSize = 18.sp,
//            modifier = Modifier.weight(1f)
//        )
//
//        // Иконка звезды
//        Icon(
//            imageVector = Icons.Filled.StarBorder,
//            contentDescription = "Favorite",
//            tint = Color.Gray
//        )
//    }
//}
//
//@Composable
//fun QuestionScreen() {
//    // Вопросы и ответы экрана
//    var currentQuestionIndex by remember { mutableStateOf(1) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        // Панель навигации
//        QuestionNavigationPanel(currentQuestionIndex)
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Заголовок с номером билета
//        Text(
//            text = "Билет $currentQuestionIndex",
//            fontSize = 20.sp,
//            fontWeight = FontWeight.Bold
//        )
//
//        Spacer(modifier = Modifier.height(32.dp))
//
//        // Вопрос
//        Text(
//            text = "Вопрос",
//            fontSize = 24.sp,
//            fontWeight = FontWeight.Bold
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Ответы
//        AllQuestionsAnswerButton("Ответ 1")
//        AllQuestionsAnswerButton("Ответ 2")
//        AllQuestionsAnswerButton("Ответ 3")
//        AllQuestionsAnswerButton("Ответ 4")
//
//        Spacer(modifier = Modifier.height(32.dp))
//
//        // Навигационные кнопки
//        Row(
//            horizontalArrangement = Arrangement.SpaceBetween,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            IconButton(onClick = { /* Handle previous question */ }) {
//                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Previous")
//            }
//            IconButton(onClick = { /* Handle next question */ }) {
//                Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = "Next")
//            }
//        }
//    }
//}
//
//@Composable
//fun QuestionNavigationPanel(currentQuestionIndex: Int) {
//    Row(
//        horizontalArrangement = Arrangement.spacedBy(8.dp),
//        modifier = Modifier.fillMaxWidth(),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        IconButton(onClick = { /* Handle pause/play */ }) {
//            Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Play/Pause")
//        }
//        (1..8).forEach { index ->
//            Text(
//                text = "$index",
//                fontSize = 18.sp,
//                modifier = Modifier
//                    .padding(4.dp)
//                    .clickable { /* Handle question navigation */ },
//                color = if (index == currentQuestionIndex) Color.Black else Color.Gray
//            )
//        }
//    }
//}
//
//@Composable
//fun AllQuestionsAnswerButton(text: String) {
//    Button(
//        onClick = { /* Handle answer selection */ },
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp),
//        shape = RoundedCornerShape(8.dp)
//    ) {
//        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun PreviewAllQuestionsScreen() {
//    val navController = rememberNavController()
//    AllQuestionsScreen(navController = navController)
//}
