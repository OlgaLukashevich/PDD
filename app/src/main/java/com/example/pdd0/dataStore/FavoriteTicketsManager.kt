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
}
