package com.example.pdd0.utils

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
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
                Text(
                    text = "Пауза",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ) // Увеличиваем размер и делаем жирным
                )
            },
            text = {
                Column(
//                    modifier = Modifier.background(Color(0xFFE0F7FA)) // Сетевой цвет фона: светло-голубой
                ) {
                    TextButton(
                        onClick = onResume,
                        modifier = Modifier.fillMaxWidth() // Кнопка на всю ширину
                    ) {
                        Text(
                            text = "Продолжить",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            ) // Увеличиваем размер текста
                        )
                    }
                    TextButton(
                        onClick = {
                            navController.navigate("main_screen") // Переход на главный экран
                            viewModel.resetTimerToInitial()
                        },
                        modifier = Modifier.fillMaxWidth() // Кнопка на всю ширину
                    ) {
                        Text(
                            text = "На главную",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            ) // Увеличиваем размер текста
                        )
                    }
                    // Кнопка добавления в избранное
                    TextButton(
                        onClick = {
                            Log.d(
                                "PauseDialog",
                                "Добавляю билет в избранное: $currentTicketNumber"
                            ) // ✅ Логируем
                            onAddToFavorites()
                            onResume()
                        },
                        modifier = Modifier.fillMaxWidth() // Кнопка на всю ширину
                    ) {
                        Text(
                            text = if (isFavorite) "Удалить из избранного" else "Добавить в избранное",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            ) // Увеличиваем размер текста
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onResume) {
                    Text(
                        text = "Закрыть",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        ) // Увеличиваем размер текста
                    )
                }
            }
        )
    }
