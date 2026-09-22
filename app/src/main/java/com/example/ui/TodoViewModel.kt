package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Priority
import com.example.data.TodoDatabase
import com.example.data.TodoItem
import com.example.data.TodoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TodoFilter(val label: String) {
    ALL("All"),
    PENDING("Pending"),
    COMPLETED("Done")
}

data class TodoUiState(
    val filteredTodos: List<TodoItem> = emptyList(),
    val totalCount: Int = 0,
    val pendingCount: Int = 0,
    val completedCount: Int = 0,
    val currentFilter: TodoFilter = TodoFilter.ALL
)

class TodoViewModel(
    application: Application,
    private val repository: TodoRepository
) : AndroidViewModel(application) {

    private val _currentFilter = MutableStateFlow(TodoFilter.ALL)

    val uiState: StateFlow<TodoUiState> = combine(
        repository.allTodos,
        _currentFilter
    ) { allTodos, filter ->
        val total = allTodos.size
        val completed = allTodos.count { it.isCompleted }
        val pending = total - completed

        val filtered = when (filter) {
            TodoFilter.ALL -> allTodos
            TodoFilter.PENDING -> allTodos.filter { !it.isCompleted }
            TodoFilter.COMPLETED -> allTodos.filter { it.isCompleted }
        }

        TodoUiState(
            filteredTodos = filtered,
            totalCount = total,
            pendingCount = pending,
            completedCount = completed,
            currentFilter = filter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TodoUiState()
    )

    fun setFilter(filter: TodoFilter) {
        _currentFilter.value = filter
    }

    fun addTodo(title: String, priority: Priority = Priority.MEDIUM) {
        val trimmed = title.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            repository.insert(
                TodoItem(
                    title = trimmed,
                    priority = priority,
                    isCompleted = false
                )
            )
        }
    }

    fun toggleComplete(todo: TodoItem) {
        viewModelScope.launch {
            repository.update(todo.copy(isCompleted = !todo.isCompleted))
        }
    }

    fun deleteTodo(todo: TodoItem) {
        viewModelScope.launch {
            repository.delete(todo)
        }
    }

    fun clearCompleted() {
        viewModelScope.launch {
            repository.clearCompleted()
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = TodoDatabase.getDatabase(application)
                    val repo = TodoRepository(db.todoDao())
                    return TodoViewModel(application, repo) as T
                }
            }
    }
}
