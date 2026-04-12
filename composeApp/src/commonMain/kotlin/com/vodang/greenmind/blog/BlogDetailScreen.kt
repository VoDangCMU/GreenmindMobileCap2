package com.vodang.greenmind.blog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.blog.BlogDto
import com.vodang.greenmind.api.blog.getBlog
import com.vodang.greenmind.api.blog.toggleBlogLike
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.platform.BackHandler
import kotlinx.coroutines.launch

// ── Palette ───────────────────────────────────────────────────────────────────

private val dGreen800  = Color(0xFF2E7D32)
private val dGreen700  = Color(0xFF388E3C)
private val dGreen600  = Color(0xFF43A047)
private val dGreen50   = Color(0xFFE8F5E9)
private val dGreen100  = Color(0xFFC8E6C9)
private val dGray700   = Color(0xFF374151)
private val dGray500   = Color(0xFF6B7280)
private val dGray400   = Color(0xFF9CA3AF)
private val dRed500    = Color(0xFFEF4444)
private val dBgGray    = Color(0xFFF3F4F6)

private val tagColorPairs = listOf(
    Color(0xFF1D4ED8) to Color(0xFFEFF6FF),
    Color(0xFF065F46) to Color(0xFFECFDF5),
    Color(0xFF92400E) to Color(0xFFFEF3C7),
    Color(0xFF7C3AED) to Color(0xFFF5F3FF),
    Color(0xFF9F1239) to Color(0xFFFFF1F2),
)
private fun tagPair(i: Int) = tagColorPairs[i % tagColorPairs.size]

// ── Helpers ───────────────────────────────────────────────────────────────────

/** Strips HTML tags and decodes basic entities for plain-text rendering. */
internal fun stripHtml(html: String): String =
    html
        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("<p[^>]*>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("<h[1-6][^>]*>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("</h[1-6]>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("<li[^>]*>", RegexOption.IGNORE_CASE), "\n• ")
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

private fun initials(fullName: String, username: String) =
    fullName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("").ifBlank { username.take(2).uppercase() }

private fun readMinutes(content: String?) = ((content?.split(" ")?.size ?: 0) / 200).coerceAtLeast(1)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun BlogDetailScreen(
    blogId: String,
    accessToken: String,
    onBack: () -> Unit,
) {
    // Intercept system back gesture — navigate to list, not home
    BackHandler(onBack = onBack)

    val s     = LocalAppStrings.current
    val scope = rememberCoroutineScope()

    var blog      by remember { mutableStateOf<BlogDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error     by remember { mutableStateOf<String?>(null) }
    var likeCount by remember { mutableStateOf(0) }
    var isLiked   by remember { mutableStateOf(false) }
    var isLiking  by remember { mutableStateOf(false) }

    LaunchedEffect(blogId) {
        isLoading = true; error = null
        try {
            val result = getBlog(blogId, accessToken)
            blog      = result.data
            likeCount = result.data.likeCount
            isLiked   = result.data.liked
        } catch (e: Throwable) {
            error = e.message ?: s.blogErrorLoad
        }
        isLoading = false
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
                            isLiked   = result.data.liked
                        } catch (e: Throwable) { error = e.message ?: s.blogErrorLoad }
                        isLoading = false
                    }
                },
            )

            blog != null -> ArticleContent(
                post      = blog!!,
                likeCount = likeCount,
                isLiked   = isLiked,
                isLiking  = isLiking,
                onLike    = {
                    if (isLiking) return@ArticleContent
                    scope.launch {
                        isLiking = true
                        val wasLiked = isLiked
                        // Optimistic update — flip immediately so the UI responds
                        isLiked   = !wasLiked
                        likeCount = (likeCount + if (!wasLiked) 1 else -1).coerceAtLeast(0)
                        try {
                            val result = toggleBlogLike(blog!!.id, accessToken)
                            // Sync count from server; trust optimistic liked state
                            if (result.likeCount > 0) likeCount = result.likeCount
                        } catch (_: Throwable) {
                            // Revert on network error
                            isLiked   = wasLiked
                            likeCount = (likeCount + if (wasLiked) 1 else -1).coerceAtLeast(0)
                        }
                        isLiking = false
                    }
                },
            )
        }
    }
}

// ── Article content ───────────────────────────────────────────────────────────

@Composable
private fun ArticleContent(
    post: BlogDto,
    likeCount: Int,
    isLiked: Boolean,
    isLiking: Boolean,
    onLike: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
        ) {
            // ── Hero ─────────────────────────────────────────────────────────
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(dGreen800, dGreen700)))
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Tags
                        if (post.tags.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                post.tags.take(4).forEachIndexed { i, tag ->
                                    val (fg, bg) = tagPair(i)
                                    Box(
                                        Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color.White.copy(alpha = 0.18f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(tag, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                    }
                                }
                            }
                        }

                        // Title
                        Text(
                            post.title,
                            fontSize   = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = Color.White,
                            lineHeight = 32.sp,
                        )

                        // Author row
                        val avInitials = initials(post.author?.fullName ?: "", post.author?.username ?: "?")
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                Modifier.size(38.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(avInitials, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text(
                                    post.author?.fullName ?: post.author?.username ?: post.authorId,
                                    fontSize   = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = Color.White,
                                )
                                Text(
                                    formatDate(post.createdAt),
                                    fontSize = 11.sp,
                                    color    = Color.White.copy(alpha = 0.7f),
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            // Read time
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    "🕐 ${readMinutes(post.content)} min read",
                                    fontSize = 11.sp,
                                    color    = Color.White.copy(alpha = 0.9f),
                                )
                            }
                        }
                    }
                }
            }

            // ── Content body ──────────────────────────────────────────────────
            item {
                val contentText = post.content?.let { stripHtml(it) }?.ifBlank { null }

                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-16).dp),
                    shape  = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(
                        Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        if (contentText != null) {
                            // Render paragraphs separately for nicer spacing
                            val paragraphs = contentText.split("\n\n").filter { it.isNotBlank() }
                            paragraphs.forEach { para ->
                                val trimmed = para.trim()
                                when {
                                    trimmed.startsWith("•") -> {
                                        // Bullet list item
                                        Text(
                                            trimmed,
                                            fontSize   = 15.sp,
                                            color      = dGray700,
                                            lineHeight = 24.sp,
                                        )
                                    }
                                    trimmed.length < 80 && !trimmed.contains('.') -> {
                                        // Likely a heading-ish short line
                                        Text(
                                            trimmed,
                                            fontSize   = 17.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color      = Color(0xFF111827),
                                            lineHeight = 26.sp,
                                        )
                                    }
                                    else -> {
                                        Text(
                                            trimmed,
                                            fontSize   = 15.sp,
                                            color      = dGray700,
                                            lineHeight = 26.sp,
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                "No content available.",
                                fontSize = 15.sp,
                                color    = dGray400,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }

            // ── Stats row ─────────────────────────────────────────────────────
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-8).dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatChip(icon = "📖", label = "${readMinutes(post.content)} min read")
                    if (post.tags.isNotEmpty()) {
                        StatChip(icon = "🏷", label = "${post.tags.size} tags")
                    }
                }
            }
        }

        // ── Sticky like icon ──────────────────────────────────────────────────
        Row(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, dBgGray.copy(alpha = 0.95f), dBgGray)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            IconButton(onClick = onLike, enabled = !isLiking) {
                if (isLiking) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = dGreen800,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        "♥",
                        fontSize = 26.sp,
                        color    = if (isLiked) dRed500 else dGray400,
                    )
                }
            }
            Text(
                "$likeCount",
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = if (isLiked) dRed500 else dGray500,
            )
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

// ── Stat chip ─────────────────────────────────────────────────────────────────

@Composable
private fun StatChip(icon: String, label: String) {
    Box(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(icon, fontSize = 11.sp)
            Text(label, fontSize = 11.sp, color = dGray500, fontWeight = FontWeight.Medium)
        }
    }
}

// ── Error state ───────────────────────────────────────────────────────────────

@Composable
private fun ErrorState(
    message: String,
    retryLabel: String,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("😕", fontSize = 40.sp)
            Text(message, fontSize = 14.sp, color = dGray500, textAlign = TextAlign.Center)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    shape   = RoundedCornerShape(10.dp),
                ) { Text("← Back") }
                Button(
                    onClick = onRetry,
                    colors  = ButtonDefaults.buttonColors(containerColor = dGreen800),
                    shape   = RoundedCornerShape(10.dp),
                ) { Text(retryLabel) }
            }
        }
    }
}
