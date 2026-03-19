package com.vodang.greenmind.store

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class TodoItem(
    val id: String,
    val title: String,
    val done: Boolean = false,
    val children: List<TodoItem> = emptyList()
)

fun TodoItem.countAll(): Int = 1 + children.sumOf { it.countAll() }
fun TodoItem.countDone(): Int = (if (done) 1 else 0) + children.sumOf { it.countDone() }

// ── Tree helpers ──────────────────────────────────────────────────────────────

private fun TodoItem.withChildrenSetTo(done: Boolean): TodoItem =
    copy(done = done, children = children.map { it.withChildrenSetTo(done) })

private fun List<TodoItem>.containsId(id: String): Boolean =
    any { it.id == id || it.children.containsId(id) }

private fun List<TodoItem>.toggleById(id: String): List<TodoItem> = map { item ->
    when {
        item.id == id -> item.withChildrenSetTo(!item.done)
        item.children.containsId(id) -> {
            val newChildren = item.children.toggleById(id)
            item.copy(children = newChildren, done = newChildren.isNotEmpty() && newChildren.all { it.done })
        }
        else -> item
    }
}

private fun List<TodoItem>.deleteById(id: String): List<TodoItem> =
    filter { it.id != id }.map { it.copy(children = it.children.deleteById(id)) }

private fun List<TodoItem>.addChildTo(parentId: String, child: TodoItem): List<TodoItem> = map { item ->
    if (item.id == parentId) item.copy(children = item.children + child)
    else item.copy(children = item.children.addChildTo(parentId, child))
}

// ── Store ─────────────────────────────────────────────────────────────────────

private const val KEY_TODOS = "todos"

object TodoStore {

    private val settings: Settings = Settings()
    private val json = Json { ignoreUnknownKeys = true }

    private val _todos = MutableStateFlow<List<TodoItem>>(
        settings.getStringOrNull(KEY_TODOS)
            ?.let { runCatching { json.decodeFromString<List<TodoItem>>(it) }.getOrNull() }
            ?: emptyList()
    )

    val todos: StateFlow<List<TodoItem>> = _todos.asStateFlow()

    private fun persist() {
        settings.putString(KEY_TODOS, json.encodeToString(_todos.value))
    }

    private fun newId() = kotlin.random.Random.nextLong().toString()

    /** Add a root-level item when [parentId] is null, otherwise add as child. */
    fun addItem(parentId: String?, title: String) {
        val child = TodoItem(id = newId(), title = title)
        _todos.value = if (parentId == null) {
            _todos.value + child
        } else {
            _todos.value.addChildTo(parentId, child)
        }
        persist()
    }

    fun deleteItem(id: String) {
        _todos.value = _todos.value.deleteById(id)
        persist()
    }

    fun toggleItem(id: String) {
        _todos.value = _todos.value.toggleById(id)
        persist()
    }
}
