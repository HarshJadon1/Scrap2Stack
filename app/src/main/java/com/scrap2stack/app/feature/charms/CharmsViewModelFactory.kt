package com.scrap2stack.app.feature.charms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.scrap2stack.app.domain.repository.CharmsRepository

class CharmsViewModelFactory(
    private val charmsRepository: CharmsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CharmsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CharmsViewModel(charmsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
