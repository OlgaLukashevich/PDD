package com.example.pdd0

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.Color

class QuestionScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuestionScreen()
        }
    }
}

@Composable
fun QuestionScreen() {
    var currentQuestionIndex by remember { mutableStateOf(21) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Панель навигации
        QuestionNavigationPanel(currentQuestionIndex)

        Spacer(modifier = Modifier.height(16.dp))

        // Заголовок с номером билета
        Text(
            text = "Билет $currentQuestionIndex",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Вопрос
        Text(
            text = "Вопрос",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Ответы
        AnswerButton("Ответ 1")
        AnswerButton("Ответ 2")
        AnswerButton("Ответ 3")
        AnswerButton("Ответ 4")

        Spacer(modifier = Modifier.height(32.dp))

        // Навигационные кнопки
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
fun QuestionNavigationPanel(currentQuestionIndex: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { /* Handle pause/play */ }) {
            Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Play/Pause")
        }
        (1..8).forEach { index ->
            Text(
                text = "$index",
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(4.dp)
                    .clickable { /* Handle question navigation */ },
                color = if (index == currentQuestionIndex) Color.Black else Color.Gray
            )
        }
    }
}

@Composable
fun AnswerButton(text: String) {
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