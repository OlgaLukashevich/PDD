package com.example.pdd0

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pdd0.dataStore.FavoriteTicketsManager

class QuestionViewModelFactory(
    private val favoriteTicketsManager: FavoriteTicketsManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return QuestionViewModel(favoriteTicketsManager) as T
    }
}