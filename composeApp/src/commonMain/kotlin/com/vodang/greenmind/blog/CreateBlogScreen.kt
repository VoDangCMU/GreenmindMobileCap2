package com.vodang.greenmind.blog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.blog.CreateBlogRequest
import com.vodang.greenmind.api.blog.createBlog
import com.vodang.greenmind.i18n.LocalAppStrings
import kotlinx.coroutines.launch

private val green800 = Color(0xFF2E7D32)

@Composable
fun CreateBlogScreen(
    accessToken: String,
    onBack: () -> Unit,
    onCreated: () -> Unit,
) {
    val s = LocalAppStrings.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var tagsRaw by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        // Top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            TextButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart),
                enabled = !isSubmitting,
            ) {
                Text("← ${s.back}", color = green800, fontSize = 13.sp)
            }
            Text(
                s.blogCreate,
                modifier = Modifier.align(Alignment.Center),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Title field
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(s.blogCreateTitle, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF424242))
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 500) title = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(s.blogCreateTitleHint, fontSize = 14.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = green800,
                        unfocusedBorderColor = Color(0xFFDDDDDD),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                    ),
                    supportingText = { Text("${title.length}/500", fontSize = 11.sp, color = Color.Gray) },
                )
            }

            // Content field
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(s.blogCreateContent, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF424242))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
                    placeholder = { Text(s.blogCreateContentHint, fontSize = 14.sp) },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = green800,
                        unfocusedBorderColor = Color(0xFFDDDDDD),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                    ),
                    minLines = 8,
                )
            }

            // Tags field
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(s.blogCreateTags, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF424242))
                OutlinedTextField(
                    value = tagsRaw,
                    onValueChange = { tagsRaw = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(s.blogCreateTagsHint, fontSize = 14.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = green800,
                        unfocusedBorderColor = Color(0xFFDDDDDD),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                    ),
                )
            }

            // Error
            if (error != null) {
                Text(
                    error!!,
                    fontSize = 13.sp,
                    color = Color(0xFFC62828),
                )
            }

            // Submit button
            Button(
                onClick = {
                    val trimTitle = title.trim()
                    val trimContent = content.trim()
                    if (trimTitle.isEmpty() || trimContent.isEmpty()) {
                        error = s.blogCreateErrorEmpty
                        return@Button
                    }
                    val tags = tagsRaw.split(',')
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    scope.launch {
                        isSubmitting = true
                        error = null
                        try {
                            createBlog(
                                accessToken = accessToken,
                                request = CreateBlogRequest(
                                    title = trimTitle,
                                    content = trimContent,
                                    tags = tags,
                                )
                            )
                            onCreated()
                        } catch (e: Throwable) {
                            error = e.message ?: s.blogErrorLoad
                        }
                        isSubmitting = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = green800),
                shape = RoundedCornerShape(10.dp),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    if (isSubmitting) s.blogCreateSubmitting else s.blogCreateSubmit,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
