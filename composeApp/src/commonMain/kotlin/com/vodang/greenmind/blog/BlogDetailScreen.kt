package com.vodang.greenmind.blog

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.blog.CommentDto
import com.vodang.greenmind.api.blog.BlogDto
import com.vodang.greenmind.api.blog.addComment
import com.vodang.greenmind.api.blog.deleteComment
import com.vodang.greenmind.api.blog.deleteBlog
import com.vodang.greenmind.api.blog.getBlog
import com.vodang.greenmind.api.blog.toggleBlogLike
import com.vodang.greenmind.api.blog.updateComment
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.platform.BackHandler
import com.vodang.greenmind.store.SettingsStore
import kotlinx.coroutines.launch

// -- Palette (green scheme preserved) ------------------------------------------

private val dGreen800 = Color(0xFF2E7D32)
private val dGreen100 = Color(0xFFC8E6C9)
private val dGray700  = Color(0xFF374151)
private val dGray500  = Color(0xFF6B7280)
private val dGray400  = Color(0xFF9CA3AF)
private val dRed500   = Color(0xFFE53935)
private val dBgGray   = Color(0xFFEEF0F3)
private val dDivider  = Color(0xFFE4E6EB)
private val dGray100  = Color(0xFFF3F4F6)

// -- Helpers -------------------------------------------------------------------

internal fun stripHtml(html: String): String =
    html
        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("<p[^>]*>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("<h[1-6][^>]*>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("</h[1-6]>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("<li[^>]*>", RegexOption.IGNORE_CASE), "\n- ")
        .replace(Regex("<img[^>]*/?>", RegexOption.IGNORE_CASE), "\n[image]\n")
        .replace(Regex("<[^>]+>"), "")
        .replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
        .replace("&nbsp;", " ").replace("&quot;", "\"").replace("&#39;", "'")
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()

private fun formatDate(iso: String): String = try {
    val parts = iso.substringBefore('T').split('-')
    if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else iso
} catch (_: Throwable) { iso }

private fun formatDateTime(iso: String): String = try {
    val datePart = iso.substringBefore('T')
    val timePart = iso.substringAfter('T').take(5)
    val dp = datePart.split('-')
    if (dp.size == 3) "${dp[2]}/${dp[1]}/${dp[0]} $timePart" else iso
} catch (_: Throwable) { iso }

private fun initials(fullName: String, username: String) =
    fullName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("").ifBlank { username.take(2).uppercase() }

private fun readMinutes(content: String?) = ((content?.split(" ")?.size ?: 0) / 200).coerceAtLeast(1)

// -- Screen --------------------------------------------------------------------

@Composable
fun BlogDetailScreen(
    blogId: String,
    accessToken: String,
    onBack: () -> Unit,
    onBlogLiked: (String, Boolean, Int) -> Unit = { _, _, _ -> },
    onBlogDeleted: () -> Unit = {},
) {
    BackHandler(onBack = onBack)

    val s     = LocalAppStrings.current
    val scope = rememberCoroutineScope()
    val currentUser = SettingsStore.getUser()

    var blog      by remember { mutableStateOf<BlogDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error     by remember { mutableStateOf<String?>(null) }
    var likeCount by remember { mutableStateOf(0) }
    var isLiked   by remember { mutableStateOf(false) }
    var isLiking  by remember { mutableStateOf(false) }
    var showMenu  by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    fun loadBlog() {
        scope.launch {
            isLoading = true; error = null
            try {
                val result = getBlog(blogId, accessToken)
                blog      = result.data
                likeCount = result.data.likeCount
                isLiked   = result.data.isLiked || result.data.liked
            } catch (e: Throwable) {
                error = e.message ?: s.blogErrorLoad
            }
            isLoading = false
        }
    }

    LaunchedEffect(blogId) { loadBlog() }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(s.blogDeleteTitle) },
            text = { Text(s.blogDeleteConfirm) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            isDeleting = true
                            try {
                                deleteBlog(blogId, accessToken)
                                showDeleteDialog = false
                                onBlogDeleted()
                            } catch (e: Throwable) {
                                error = e.message
                                isDeleting = false
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = dRed500),
                ) {
                    if (isDeleting) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    else Text(s.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(s.cancel) }
            },
        )
    }

    Box(Modifier.fillMaxSize().background(dBgGray)) {
        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = dGreen800)
            }

            error != null -> ErrorState(
                message = error!!,
                retryLabel = s.blogRetry,
                onBack = onBack,
                onRetry = {
                    scope.launch {
                        isLoading = true; error = null
                        try {
                            val result = getBlog(blogId, accessToken)
                            blog      = result.data
                            likeCount = result.data.likeCount
                            isLiked   = result.data.isLiked || result.data.liked
                        } catch (e: Throwable) { error = e.message ?: s.blogErrorLoad }
                        isLoading = false
                    }
                },
            )

            blog != null -> PostDetail(
                post      = blog!!,
                likeCount = likeCount,
                isLiked   = isLiked,
                isLiking  = isLiking,
                onBack    = onBack,
                onLike    = {
                    if (isLiking) return@PostDetail
                    scope.launch {
                        isLiking = true
                        val wasLiked = isLiked
                        isLiked   = !wasLiked
                        likeCount = (likeCount + if (!wasLiked) 1 else -1).coerceAtLeast(0)
                        try {
                            val result = toggleBlogLike(blog!!.id, accessToken)
                            if (result.likeCount > 0) likeCount = result.likeCount
                            onBlogLiked(blog!!.id, isLiked, likeCount)
                        } catch (_: Throwable) {
                            isLiked   = wasLiked
                            likeCount = (likeCount + if (wasLiked) 1 else -1).coerceAtLeast(0)
                        }
                        isLiking = false
                    }
                },
                currentUserId = currentUser?.id,
                onDeleteClick = { showDeleteDialog = true },
                onCommentAdded = { newComment ->
                    blog = blog?.copy(
                        comments = blog!!.comments + newComment,
                        commentCount = blog!!.commentCount + 1,
                    )
                },
                onCommentUpdated = { commentId, content ->
                    blog = blog?.copy(
                        comments = blog!!.comments.map {
                            if (it.id == commentId) it.copy(content = content, updatedAt = isoNow())
                            else it
                        },
                    )
                },
                onCommentDeleted = { commentId ->
                    blog = blog?.copy(
                        comments = blog!!.comments.filter { it.id != commentId },
                        commentCount = blog!!.commentCount - 1,
                    )
                },
                accessToken = accessToken,
                showMenu = showMenu,
                onShowMenu = { showMenu = it },
            )
        }
    }
}

private fun isoNow(): String {
    val now = java.time.Instant.now()
    return now.toString()
}

// -- Post detail (Facebook-style) ----------------------------------------------

@Composable
private fun PostDetail(
    post: BlogDto,
    likeCount: Int,
    isLiked: Boolean,
    isLiking: Boolean,
    onBack: () -> Unit,
    onLike: () -> Unit,
    currentUserId: String?,
    onDeleteClick: () -> Unit,
    onCommentAdded: (CommentDto) -> Unit,
    onCommentUpdated: (String, String) -> Unit,
    onCommentDeleted: (String) -> Unit,
    accessToken: String,
    showMenu: Boolean,
    onShowMenu: (Boolean) -> Unit,
) {
    val s = LocalAppStrings.current
    val isOwner = currentUserId == post.authorId
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        // -- Back bar ------------------------------------------------------
        item {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onBack) {
                        Text(s.back, color = dGreen800, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    if (isOwner) {
                        Box {
                            IconButton(onClick = { onShowMenu(!showMenu) }) {
                                Text("\u22EE", fontSize = 20.sp, color = dGray500)
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { onShowMenu(false) },
                            ) {
                                DropdownMenuItem(
                                    text = { Text(s.blogEdit, color = dGray700) },
                                    onClick = { onShowMenu(false) },
                                )
                                DropdownMenuItem(
                                    text = { Text(s.blogDelete, color = dRed500) },
                                    onClick = {
                                        onShowMenu(false)
                                        onDeleteClick()
                                    },
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // -- Post card (full-width white block) ----------------------------
        item {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                Column {
                    // -- Author header -------------------------------------
                    val avInitials = initials(post.author?.fullName ?: "", post.author?.username ?: "?")
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            Modifier.size(44.dp).clip(CircleShape).background(dGreen100),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(avInitials, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = dGreen800)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                post.author?.fullName ?: post.author?.username ?: post.authorId,
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = Color(0xFF050505),
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(formatDate(post.createdAt), fontSize = 12.sp, color = dGray500)
                                Text("\u00B7", fontSize = 12.sp, color = dGray500)
                                Text("\uD83D\uDD50 ${readMinutes(post.content)} min read", fontSize = 12.sp, color = dGray500)
                            }
                        }
                    }

                    // -- Title ---------------------------------------------
                    Text(
                        post.title,
                        modifier   = Modifier.padding(horizontal = 16.dp),
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color(0xFF050505),
                        lineHeight = 28.sp,
                    )

                    Spacer(Modifier.height(8.dp))

                    // -- Content body --------------------------------------
                    val contentText = post.content?.let { stripHtml(it) }?.ifBlank { null }

                    if (contentText != null) {
                        val paragraphs = contentText.split("\n\n").filter { it.isNotBlank() }
                        Column(
                            Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            paragraphs.forEach { para ->
                                val trimmed = para.trim()
                                when {
                                    trimmed.startsWith("- ") -> {
                                        Text(trimmed, fontSize = 15.sp, color = dGray700, lineHeight = 24.sp)
                                    }
                                    trimmed.length < 80 && !trimmed.contains('.') -> {
                                        Text(
                                            trimmed,
                                            fontSize   = 17.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color      = Color(0xFF111827),
                                            lineHeight = 26.sp,
                                        )
                                    }
                                    else -> {
                                        Text(trimmed, fontSize = 15.sp, color = dGray700, lineHeight = 26.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            s.noContentAvailable,
                            modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            fontSize  = 15.sp,
                            color     = dGray400,
                            textAlign = TextAlign.Center,
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // -- Tags (as #hashtags) -------------------------------
                    if (post.tags.isNotEmpty()) {
                        Row(
                            Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            post.tags.forEach { tag ->
                                Text("#$tag", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = dGreen800)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    // -- Like & comment count summary ----------------------
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (likeCount > 0) {
                            Text("\u2665", fontSize = 15.sp, color = if (isLiked) dRed500 else dGray500)
                            Text("$likeCount", fontSize = 14.sp, color = dGray500)
                        }
                        if (post.commentCount > 0) {
                            if (likeCount > 0) Text("\u00B7", fontSize = 14.sp, color = dGray500)
                            Text("${post.commentCount} ${s.blogCommentsLabel}", fontSize = 14.sp, color = dGray500)
                        }
                    }

                    // -- Divider -------------------------------------------
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), thickness = 1.dp, color = dDivider)

                    // -- Action bar: Like ----------------------------------
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        TextButton(
                            onClick = onLike,
                            enabled = !isLiking,
                            modifier = Modifier.weight(1f),
                        ) {
                            if (isLiking) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = dGreen800, strokeWidth = 2.dp)
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        if (isLiked) "\u2665" else "\u2661",
                                        fontSize = 20.sp,
                                        color = if (isLiked) dRed500 else dGray500,
                                    )
                                    Text(
                                        if (isLiked) s.blogLiked else s.blogLike,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isLiked) dRed500 else dGray500,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // -- Add comment input --------------------------------------------
        item {
            AddCommentSection(
                accessToken = accessToken,
                blogId = post.id,
                onCommentAdded = onCommentAdded,
            )
            Spacer(Modifier.height(8.dp))
        }

        // -- Comments list ------------------------------------------------
        if (post.comments.isNotEmpty()) {
            item {
                Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            s.blogCommentsTitle(post.commentCount),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = dGray700,
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            items(post.comments, key = { it.id }) { comment ->
                CommentItem(
                    comment = comment,
                    currentUserId = currentUserId,
                    blogId = post.id,
                    accessToken = accessToken,
                    onUpdated = onCommentUpdated,
                    onDeleted = onCommentDeleted,
                )
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

// -- Add comment section -------------------------------------------------------

@Composable
private fun AddCommentSection(
    accessToken: String,
    blogId: String,
    onCommentAdded: (CommentDto) -> Unit,
) {
    val s = LocalAppStrings.current
    val user = SettingsStore.getUser()
    val scope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val initials = initials(user?.fullName ?: "", user?.username ?: "?")
            Box(
                Modifier.size(32.dp).clip(CircleShape).background(dGreen100),
                contentAlignment = Alignment.Center,
            ) {
                Text(initials, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = dGreen800)
            }
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= 500) text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(s.blogAddCommentHint, fontSize = 14.sp, color = dGray400) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = dGreen800,
                    unfocusedBorderColor = dDivider,
                    focusedContainerColor = dGray100,
                    unfocusedContainerColor = dGray100,
                ),
            )
            IconButton(
                onClick = {
                    val trimmed = text.trim()
                    if (trimmed.isEmpty() || isSubmitting) return@IconButton
                    scope.launch {
                        isSubmitting = true
                        try {
                            val result = addComment(blogId, trimmed, accessToken)
                            onCommentAdded(result.data)
                            text = ""
                        } catch (_: Throwable) { /* silent */ }
                        isSubmitting = false
                    }
                },
                enabled = text.isNotBlank() && !isSubmitting,
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = dGreen800, strokeWidth = 2.dp)
                } else {
                    Text("\u27A4", fontSize = 18.sp, color = if (text.isNotBlank()) dGreen800 else dGray400)
                }
            }
        }
    }
}

// -- Comment item --------------------------------------------------------------

@Composable
private fun CommentItem(
    comment: CommentDto,
    currentUserId: String?,
    blogId: String,
    accessToken: String,
    onUpdated: (String, String) -> Unit,
    onDeleted: (String) -> Unit,
) {
    val s = LocalAppStrings.current
    val scope = rememberCoroutineScope()
    val isOwner = currentUserId == comment.user.id

    var showMenu by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf(comment.content) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Edit dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(s.blogEditComment) },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { if (it.length <= 500) editText = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    shape = RoundedCornerShape(8.dp),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = editText.trim()
                        if (trimmed.isEmpty()) return@TextButton
                        scope.launch {
                            isSubmitting = true
                            try {
                                val result = updateComment(blogId, comment.id, trimmed, accessToken)
                                onUpdated(comment.id, result.data.content)
                                showEditDialog = false
                            } catch (_: Throwable) { /* silent */ }
                            isSubmitting = false
                        }
                    },
                    enabled = editText.isNotBlank() && !isSubmitting,
                ) {
                    if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    else Text(s.save)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text(s.cancel) }
            },
        )
    }

    // Delete dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(s.blogDeleteCommentTitle) },
            text = { Text(s.blogDeleteCommentConfirm) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            isSubmitting = true
                            try {
                                deleteComment(blogId, comment.id, accessToken)
                                onDeleted(comment.id)
                                showDeleteDialog = false
                            } catch (_: Throwable) { /* silent */ }
                            isSubmitting = false
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = dRed500),
                ) {
                    if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    else Text(s.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(s.cancel) }
            },
        )
    }

    Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val initials = initials(comment.user.fullName, comment.user.username)
            Box(
                Modifier.size(32.dp).clip(CircleShape).background(dGreen100),
                contentAlignment = Alignment.Center,
            ) {
                Text(initials, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = dGreen800)
            }
            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        comment.user.fullName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF050505),
                    )
                    Text(formatDateTime(comment.createdAt), fontSize = 11.sp, color = dGray400)
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    comment.content,
                    fontSize = 14.sp,
                    color = dGray700,
                    lineHeight = 20.sp,
                )
            }
            if (isOwner) {
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(28.dp),
                    ) {
                        Text("\u22EE", fontSize = 18.sp, color = dGray400)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(s.blogEdit, color = dGray700) },
                            onClick = {
                                showMenu = false
                                editText = comment.content
                                showEditDialog = true
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(s.blogDelete, color = dRed500) },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            },
                        )
                    }
                }
            }
        }
    }
}

// -- Error state ---------------------------------------------------------------

@Composable
private fun ErrorState(
    message: String,
    retryLabel: String,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    val s = LocalAppStrings.current
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("\uD83D\uDE15", fontSize = 40.sp)
            Text(message, fontSize = 14.sp, color = dGray500, textAlign = TextAlign.Center)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    shape   = RoundedCornerShape(10.dp),
                ) { Text(s.back) }
                Button(
                    onClick = onRetry,
                    colors  = ButtonDefaults.buttonColors(containerColor = dGreen800),
                    shape   = RoundedCornerShape(10.dp),
                ) { Text(retryLabel) }
            }
        }
    }
}