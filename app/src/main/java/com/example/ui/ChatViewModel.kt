package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ChatMessage
import com.example.data.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = ChatRepository(database.chatMessageDao())

    val messages: StateFlow<List<ChatMessage>> = repository.allMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun sendMessage() {
        val textToSend = _inputText.value.trim()
        if (textToSend.isEmpty() || _isLoading.value) return

        _inputText.value = ""
        _errorMessage.value = null

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Save user's message
                repository.saveMessage(textToSend, isUser = true)

                // 2. Query Jimine
                repository.getStreamingResponse(textToSend)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendStarterQuestion(question: String) {
        if (_isLoading.value) return
        _inputText.value = question
        sendMessage()
    }
}
