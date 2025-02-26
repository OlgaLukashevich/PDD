package com.example.pdd0.dataClass

// Основная модель для знаков
data class TrafficSign(
    val number: String,
    val title: String,
    val image: String,
    val description: String
)

// Для разделения по категориям
data class SignsCategory(
    val warningSigns: Map<String, TrafficSign>,
    val prohibitiveSigns: Map<String, TrafficSign>,
    val prescriptiveSigns: Map<String, TrafficSign>,
    val specialInstructionsSigns: Map<String, TrafficSign>,
    val informationalSigns: Map<String, TrafficSign>,
    val serviceSigns: Map<String, TrafficSign>,
    val additionalInfoSigns: Map<String, TrafficSign>
)

// Модель для всего JSON-объекта
data class SignsData(
    val signs: SignsCategory
)
