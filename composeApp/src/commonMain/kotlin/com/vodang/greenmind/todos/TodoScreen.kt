package com.vodang.greenmind.todos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.TodoItem
import com.vodang.greenmind.store.TodoStore
import com.vodang.greenmind.store.countAll
import com.vodang.greenmind.store.countDone

private val green800 = Color(0xFF2E7D32)
private val green600 = Color(0xFF388E3C)
private val green400 = Color(0xFF66BB6A)
private val green100 = Color(0xFFC8E6C9)
private val green50  = Color(0xFFE8F5E9)
private val greenBg  = Color(0xFFF1F8E9)

@Composable
fun TodoScreen() {
    val s = LocalAppStrings.current
    val todos by TodoStore.todos.collectAsState()
    val isLoading by TodoStore.isLoading.collectAsState()
    val error by TodoStore.error.collectAsState()
    var newTodoText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { TodoStore.load() }

    val total = todos.sumOf { it.countAll() }
    val done  = todos.sumOf { it.countDone() }
    val progress = if (total == 0) 0f else done.toFloat() / total
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(greenBg)
    ) {
            // ── Header metrics ──────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(green800, green600)))
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Text(
                    "🌿 ${s.todosScreenTitle}",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    s.todosCardProgress(done, total),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }

        // ── Add todo row ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newTodoText,
                onValueChange = { newTodoText = it },
                placeholder = { Text(s.todosAddPlaceholder, fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = green600,
                    unfocusedBorderColor = green100,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(green800),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = {
                    if (newTodoText.isNotBlank()) {
                        TodoStore.addItem(null, newTodoText.trim())
                        newTodoText = ""
                    }
                }) {
                    Text("+", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ── Error banner ──────────────────────────────────────────────────────
        error?.let { msg ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFEBEE))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(msg, color = Color(0xFFB71C1C), fontSize = 13.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = { TodoStore.clearError() }) {
                    Text("✕", color = Color(0xFFB71C1C))
                }
            }
        }

        // ── List ──────────────────────────────────────────────────────────────
        if (isLoading && todos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = green800)
            }
        } else if (todos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🌱", fontSize = 48.sp)
                    Text(s.todosEmpty, color = Color.Gray, fontSize = 15.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(todos, key = { it.id }) { todo ->
                    TodoRootCard(todo)
                }
            }
        }
    }
}

@Composable
private fun TodoRootCard(item: TodoItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            TodoItemContent(item = item, depth = 0)
        }
    }
}

@Composable
private fun TodoItemContent(item: TodoItem, depth: Int) {
    val s = LocalAppStrings.current
    var expanded by remember(item.id) { mutableStateOf(false) }
    var newChildText by remember(item.id) { mutableStateOf("") }

    val indentStart = (depth * 20).dp
    val hasChildren = item.children.isNotEmpty()

    Column(modifier = Modifier.padding(start = indentStart)) {
        // ── Item row — clicking anywhere on the row expands if children exist ──
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
        ) {
            Checkbox(
                checked = item.done,
                onCheckedChange = { TodoStore.toggleItem(item.id) },
                colors = CheckboxDefaults.colors(
                    checkedColor = green800,
                    uncheckedColor = green400,
                    checkmarkColor = Color.White
                ),
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = item.title,
                modifier = Modifier.weight(1f),
                fontSize = if (depth == 0) 15.sp else 13.sp,
                fontWeight = if (depth == 0) FontWeight.SemiBold else FontWeight.Normal,
                color = if (item.done) Color.Gray else Color(0xFF1B5E20),
                textDecoration = if (item.done) TextDecoration.LineThrough else null
            )
            // Arrow — only when children exist, visual indicator of expand state
            if (hasChildren) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (expanded) green100 else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "▸",
                        fontSize = 14.sp,
                        color = green600,
                        modifier = Modifier.rotate(if (expanded) 90f else 0f)
                    )
                }
                Spacer(Modifier.width(2.dp))
            }
            // AI wand
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF3E5F5)),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    // TODO: Call AI endpoint to auto-generate subtasks for this todo item.
                    //       Expected: POST /todos/{item.id}/ai-expand  → List<TodoItem>
                    //       On success call TodoStore.addChildren(item.id, generatedChildren).
                    onClick = { /* TODO: AI auto-generate subtasks */ },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(28.dp)
                ) {
                    Text("✨", fontSize = 13.sp)
                }
            }
            Spacer(Modifier.width(2.dp))
            // Delete
            TextButton(
                onClick = { TodoStore.deleteItem(item.id) },
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text("✕", fontSize = 12.sp, color = Color(0xFFBDBDBD))
            }
        }

        // ── Children ──────────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                if (item.children.isNotEmpty()) {
                    // Vertical tree line + children
                    Row(modifier = Modifier.padding(start = 16.dp)) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .fillMaxHeight()
                                .background(green100, RoundedCornerShape(1.dp))
                        )
                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            item.children.forEach { child ->
                                TodoItemContent(item = child, depth = depth + 1)
                            }
                        }
                    }
                }

                // Add child row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = newChildText,
                        onValueChange = { newChildText = it },
                        placeholder = { Text(s.todosAddSubPlaceholder, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = green600,
                            unfocusedBorderColor = green100,
                            focusedContainerColor = green50,
                            unfocusedContainerColor = green50
                        )
                    )
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(green50),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(
                            onClick = {
                                if (newChildText.isNotBlank()) {
                                    TodoStore.addItem(item.id, newChildText.trim())
                                    newChildText = ""
                                }
                            },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Text("+", fontSize = 18.sp, color = green800, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
