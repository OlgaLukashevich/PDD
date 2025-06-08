package com.example.pdd0.dataStore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Создаем свойство dataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "favorite_tickets")

class FavoriteTicketsManager(private val context: Context) {
    // Используем stringSetPreferencesKey
    private val favoriteTicketsKey = stringSetPreferencesKey("favorite_tickets")

    private val ticketResultsKey = stringSetPreferencesKey("ticket_results")


    // Получить список избранных билетов

    val favoriteTickets: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[favoriteTicketsKey] ?: emptySet()
        }


    // Добавить билет в избранное
    suspend fun addFavoriteTicket(ticketNumber: String) {
        context.dataStore.edit { preferences ->
            val currentFavorites = preferences[favoriteTicketsKey] ?: emptySet()
            preferences[favoriteTicketsKey] = currentFavorites + ticketNumber
        }
    }

    // Удалить билет из избранного
    suspend fun removeFavoriteTicket(ticketNumber: String) {
        context.dataStore.edit { preferences ->
            val currentFavorites = preferences[favoriteTicketsKey] ?: emptySet()
            preferences[favoriteTicketsKey] = currentFavorites - ticketNumber
        }
    }


    // Сохранить результат по билету
    suspend fun saveTicketResult(ticketNumber: String, correctAnswers: Int) {
        context.dataStore.edit { preferences ->
            val results = preferences[ticketResultsKey]?.toMutableSet() ?: mutableSetOf()
            results.removeIf { it.startsWith("$ticketNumber:") }
            results.add("$ticketNumber:$correctAnswers")
            preferences[ticketResultsKey] = results
        }
    }

    suspend fun removeTicketResult(ticketNumber: String) {
        context.dataStore.edit { preferences ->
            val results = preferences[ticketResultsKey]?.toMutableSet() ?: return@edit
            results.removeIf { it.startsWith("$ticketNumber:") }
            preferences[ticketResultsKey] = results
        }
    }


    // Получить результаты всех билетов
    val ticketResults: Flow<Map<String, Int>> = context.dataStore.data.map { preferences ->
        preferences[ticketResultsKey]
            ?.associate {
                val (ticket, score) = it.split(":")
                ticket to score.toInt()
            } ?: emptyMap()
    }


}
