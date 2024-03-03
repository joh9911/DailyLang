package com.example.dailylang.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dailylang.data.ChatModel
import com.google.gson.GsonBuilder

class SecondViewModel: ViewModel() {
    private val _chatMessages = MutableLiveData<MutableList<ChatModel>>(mutableListOf())
    val chatMessages: LiveData<MutableList<ChatModel>> get() = _chatMessages

    private val _isMediaProjectionSuccess: MutableLiveData<Boolean> = MutableLiveData()
    val isMediaProjectionSuccess: LiveData<Boolean> get() = _isMediaProjectionSuccess

    fun setIsSuccess(boolean: Boolean){
        _isMediaProjectionSuccess.value = boolean
    }

    var initialPrompt: ChatModel? = null

    var isInitialized: Boolean = false

    private val _speechToTextValue: MutableLiveData<String> = MutableLiveData()
    val speechToTextValue: LiveData<String> get() = _speechToTextValue

    fun setSpeechToTextValue(string: String){
        _speechToTextValue.value = string
    }


    fun setInitPrompt(chatModel: ChatModel){
        initialPrompt = chatModel
    }

    fun addMessage(message: ChatModel) {
        val currentList = _chatMessages.value ?: mutableListOf()
        currentList.add(message)
        _chatMessages.value = currentList
    }

    fun createChatHistoryJson(): String {
        val messages = chatMessages.value ?: return "[]"
        val messagesForJson = messages.map { chatModel ->
            MessageModel(role = if (chatModel.isSent) "user" else "assistant", content = chatModel.text)
        }
        val requestPayload = RequestPayload(model = "gpt-3.5-turbo", messages = messagesForJson)

        val gson = GsonBuilder().create()
        return gson.toJson(requestPayload)
    }

}

data class MessageModel(
    val role: String,
    val content: String?
)

data class RequestPayload(
    val model: String,
    val messages: List<MessageModel>
)

class SecondViewModelModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SecondViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SecondViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}