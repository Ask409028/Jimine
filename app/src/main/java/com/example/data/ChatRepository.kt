package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.flow.Flow
import java.io.IOException

class ChatRepository(private val chatMessageDao: ChatMessageDao) {

    val allMessages: Flow<List<ChatMessage>> = chatMessageDao.getAllMessages()

    suspend fun saveMessage(content: String, isUser: Boolean): Long {
        val msg = ChatMessage(content = content, isUser = isUser)
        return chatMessageDao.insertMessage(msg)
    }

    suspend fun clearHistory() {
        chatMessageDao.clearAllMessages()
    }

    suspend fun getStreamingResponse(userPrompt: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("API Key is not configured. Please add your GEMINI_API_KEY securely in the Secrets panel in AI Studio.")
        }

        // Get past history from database to build dialog context
        val snapshot = chatMessageDao.getMessagesSnapshot()
        
        // Take the last 20 messages to keep the token payload small and clean
        val historyToInclude = if (snapshot.size > 20) {
            snapshot.takeLast(20)
        } else {
            snapshot
        }

        val contents = mutableListOf<Content>()
        for (msg in historyToInclude) {
            contents.add(
                Content(
                    parts = listOf(Part(text = msg.content)),
                    role = if (msg.isUser) "user" else "model"
                )
            )
        }

        // If the last message is not already the user prompt, add it
        if (contents.isEmpty() || contents.last().parts.firstOrNull()?.text != userPrompt) {
            contents.add(
                Content(
                    parts = listOf(Part(text = userPrompt)),
                    role = "user"
                )
            )
        }

        val systemInstructionText = "You are Jimine, an exceptionally smart, friendly, warm, and loyal pocket AI companion. " +
                "You assist the user with brainstorming, coding, summaries, and friendly casual chats. " +
                "Always respond in clean, readable text with easy on-screen formatting, and match the user's language."

        val request = GenerateContentRequest(
            contents = contents,
            systemInstruction = Content(parts = listOf(Part(text = systemInstructionText))),
            generationConfig = GenerationConfig(temperature = 0.7f)
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (replyText != null) {
                // Save assistant message
                saveMessage(replyText, false)
                return replyText
            } else {
                throw IOException("Received empty response from Jimine.")
            }
        } catch (e: Exception) {
            val errorMessage = e.localizedMessage ?: e.message ?: "Unknown error"
            throw IOException("Could not connect to Jimine API: $errorMessage", e)
        }
    }
}
