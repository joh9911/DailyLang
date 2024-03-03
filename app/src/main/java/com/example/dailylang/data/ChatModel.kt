package com.example.dailylang.data

data class ChatModel(
    val text: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val isSent: Boolean // true if sent by the user, false if received
)
