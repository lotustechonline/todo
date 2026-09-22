package com.example.data

import kotlinx.coroutines.flow.Flow

class TodoRepository(private val todoDao: TodoDao) {
    val allTodos: Flow<List<TodoItem>> = todoDao.getAllTodos()

    suspend fun insert(todo: TodoItem): Long = todoDao.insert(todo)

    suspend fun update(todo: TodoItem) = todoDao.update(todo)

    suspend fun delete(todo: TodoItem) = todoDao.delete(todo)

    suspend fun clearCompleted() = todoDao.clearCompleted()
}
