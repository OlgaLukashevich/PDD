package com.example.pdd0.utils

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.pdd0.QuestionViewModel
import com.example.pdd0.dataClass.Question


@Composable
fun PauseDialog(

    navController: NavController, // Добавляем NavController
    viewModel: QuestionViewModel, // ✅ Добавляем ViewModel для управления билетами
    onResume: () -> Unit,
    onGoHome: () -> Unit,
    onAddToFavorites: () -> Unit,
    questionList: List<Question>, // ✅ Добавили список вопросов

    currentTicketNumber: String // Номер текущего билета

) {


    val context = LocalContext.current
    val favoriteTickets by viewModel.favoriteTickets.collectAsState() // ✅ Исправлено!
    val isFavorite = favoriteTickets.contains(currentTicketNumber)


    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(text = "Пауза")
        },
        text = {
            Column {

                TextButton(onClick = onResume) {
                    Text("Продолжить")
                }
                TextButton(onClick = {
                    navController.navigate("main_screen") // Переход на главный экран
                }) {
                    Text("На главную")
                }
                // Кнопка добавления в избранное
                TextButton(onClick = {
                    Log.d("PauseDialog", "Добавляю билет в избранное: $currentTicketNumber") // ✅ Логируем
                    onAddToFavorites()
                    onResume()
                }) {
                    Text(if (isFavorite) "Удалить из избранного" else "Добавить в избранное")
                }

                TextButton(onClick = {
                    viewModel.loadRandomTicket()
                    navController.navigate("question_screen/${viewModel.currentQuestionIndex}") {
                        popUpTo("main_screen") { inclusive = false }
                    }
                }) {
                    Text("Следующий случайный билет(пройти заново не работает)")
                }

            }
        },
        confirmButton = {
            TextButton(onClick = onResume) {
                Text("Закрыть")
            }
        }
    )
}
