package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Priority(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High")
}

@Entity(tableName = "todos")
data class TodoItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val createdAt: Long = System.currentTimeMillis()
)
