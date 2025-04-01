package com.example.pdd0.utils

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
    navController: NavController, // Навигация
    viewModel: QuestionViewModel, // ViewModel для управления состоянием
    onResume: () -> Unit,
    onGoHome: () -> Unit,
    onAddToFavorites: () -> Unit,
    questionList: List<Question>, // Список вопросов
    currentTicketNumber: String // Номер текущего билета
) {
    val context = LocalContext.current
    val favoriteTickets by viewModel.favoriteTickets.collectAsState() // Избранные билеты
    val isFavorite = favoriteTickets.contains(currentTicketNumber)

    // Кнопка с одинаковым стилем
    @Composable
    fun StyledTextButton(text: String, onClick: () -> Unit) {
        TextButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth() // Кнопка на всю ширину
        ) {
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray // Темно-серый цвет для текста кнопки

                ) // Увеличиваем размер текста
            )
        }
    }

    // Основной диалог с фоном и скругленными углами
    Surface(
        modifier = Modifier
            .background(Color(0xFFB0C4DE)) // Светло-сероголубой фон
            .clip(RectangleShape),  // Скругление углов
        shape = RectangleShape // Убираем квадратный вид, добавляем скругленные углы
    ) {
        AlertDialog(
            onDismissRequest = {}, // Предотвращаем закрытие при клике вне окна
            title = {
                Text(
                    text = "Пауза",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray // Темно-серый цвет для заголовка

                    ) // Увеличиваем размер и делаем жирным
                )
            },
            text = {
                Column {
                    // Кнопка для продолжения
                    StyledTextButton(text = "Продолжить", onClick = onResume)

                    // Кнопка для перехода на главный экран
                    StyledTextButton(
                        text = "На главную",
                        onClick = {
                            viewModel.resetTest()
                            navController.navigate("main_screen") {
                                popUpTo("main_screen") {
                                    inclusive = true
                                } // ✅ Удаляем все предыдущие экраны
                            }
                            viewModel.resetTimerToInitial() // Сброс таймера
                            viewModel.resetCommentStates() // Сброс комментариев
                            //  viewModel.isCommentVisible.value = false // Отключаем отображение комментариев
                        }
                    )

                    // Кнопка добавления/удаления из избранного
                    StyledTextButton(
                        text = if (isFavorite) "Удалить из избранного" else "Добавить в избранное",
                        onClick = {
                            Log.d("PauseDialog", "Добавляю билет в избранное: $currentTicketNumber")
                            onAddToFavorites()
                            onResume() // Возвращаемся к текущему вопросу после добавления в избранное
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onResume) {
                    Text(
                        text = "Закрыть",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray // Темно-серый цвет для кнопки
                        ) // Увеличиваем размер текста
                    )
                }
            }
        )
    }
}
