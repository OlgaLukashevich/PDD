package com.example.pdd0

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.pdd0.dataClass.Question
import com.example.pdd0.parser.parseJson

@Composable
fun FavoriteQuestionScreen(navController: NavController) {
    val favoriteQuestions by remember { mutableStateOf<List<Question>>(emptyList()) }
    //val questionList = parseJson(context = LocalContext.current) // Загрузка вопросов

    // Заглушка для тестирования: обычно это будут данные из базы или другого источника
    // Если нет избранных, то показываем текст
    if (favoriteQuestions.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Избранных билетов пока нет.",
                fontSize = 18.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("main_screen") }) {
                Text(text = "Вернуться на главную")
            }
        }
    } else {
        // Тут ты можешь отобразить список избранных билетов
        // Например, с помощью LazyColumn
//        LazyColumn {
//            items(favoriteQuestions) { question ->
//                Text(text = question.title)
//            }
//        }
    }
}


