package com.example.dailylang.data

data class Result(
    val alternatives: List<Alternative>,
    val resultEndTime: String,
    val languageCode: String
)

data class Alternative(
    val transcript: String,
    val confidence: Float
)

data class ResponseData(
    val results: List<Result>,
    val totalBilledTime: String,
    val requestId: String,
    val usingLegacyModels: Boolean
)