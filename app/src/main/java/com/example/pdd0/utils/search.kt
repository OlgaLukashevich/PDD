package com.example.pdd0.utils

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SearchBar(
    ticketList: List<String>, // ✅ Список всех билетов
    onSearchResults: (List<String>) -> Unit // ✅ Передаём результаты поиска
) {
    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(ticketList) {
        onSearchResults(ticketList) // ✅ Показываем все билеты при запуске
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it.trim() // ✅ Убираем пробелы

                val filteredTickets = ticketList.filter { ticket ->
                    val normalizedTicket = ticket.replace("Билет ", "").trim() // ✅ Очищаем "Билет "

                    // ✅ Поиск по "Билет 1", "1", "01"
                    normalizedTicket.contains(searchText, ignoreCase = true) ||
                            ticket.contains(searchText, ignoreCase = true) ||
                            searchText == normalizedTicket ||
                            searchText == "Билет $normalizedTicket"
                }

                onSearchResults(filteredTickets) // ✅ Обновляем список
            },
            label = { Text("Поиск билета") },
            modifier = Modifier.weight(1f),
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
            }
        )
    }
}

