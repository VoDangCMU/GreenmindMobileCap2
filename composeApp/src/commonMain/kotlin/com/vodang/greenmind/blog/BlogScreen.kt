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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.blog.BlogDto
import com.vodang.greenmind.api.blog.LeaderboardEntryDto
import com.vodang.greenmind.api.blog.getBlogs
import com.vodang.greenmind.api.blog.getLeaderboard
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.platform.BackHandler
import com.vodang.greenmind.store.SettingsStore
import kotlinx.coroutines.launch

// ── Palette ───────────────────────────────────────────────────────────────────

private val green800  = Color(0xFF2E7D32)
private val green700  = Color(0xFF388E3C)
private val green600  = Color(0xFF43A047)
private val green50   = Color(0xFFE8F5E9)
private val green100  = Color(0xFFC8E6C9)
private val bgGray    = Color(0xFFF3F4F6)
private val gray700   = Color(0xFF374151)
private val gray500   = Color(0xFF6B7280)
private val gray400   = Color(0xFF9CA3AF)
private val gray100   = Color(0xFFF3F4F6)

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun formatDateShort(iso: String): String = try {
    val parts = iso.substringBefore('T').split('-')
    if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else iso
} catch (_: Throwable) { iso }

private fun estimateReadMinutes(content: String?): Int =
    ((content?.split(" ")?.size ?: 0) / 200).coerceAtLeast(1)

private fun authorInitials(name: String, username: String): String =
    name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
        .ifBlank { username.take(2).uppercase() }

// Tag accent colours — cycles through a small palette
private val tagColors = listOf(
    Color(0xFF1D4ED8) to Color(0xFFEFF6FF),
    Color(0xFF065F46) to Color(0xFFECFDF5),
    Color(0xFF92400E) to Color(0xFFFEF3C7),
    Color(0xFF7C3AED) to Color(0xFFF5F3FF),
    Color(0xFF9F1239) to Color(0xFFFFF1F2),
)
private fun tagPalette(index: Int) = tagColors[index % tagColors.size]

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun BlogScreen() {
    val s = LocalAppStrings.current
    val accessToken = SettingsStore.getAccessToken()

    var selectedTab    by remember { mutableIntStateOf(0) }
    var selectedBlogId by remember { mutableStateOf<String?>(null) }
    var showCreate     by remember { mutableStateOf(false) }
    var listRefreshKey by remember { mutableIntStateOf(0) }

    val token = accessToken ?: return

    // Intercept system back within sub-views so they pop back to the list
    BackHandler(enabled = showCreate) { showCreate = false }
    BackHandler(enabled = selectedBlogId != null) { selectedBlogId = null }

    if (showCreate) {
        CreateBlogScreen(
            accessToken = token,
            onBack = { showCreate = false },
            onCreated = { showCreate = false; listRefreshKey++ },
        )
        return
    }
    val blogId = selectedBlogId
    if (blogId != null) {
        BlogDetailScreen(
            blogId = blogId,
            accessToken = token,
            onBack = { selectedBlogId = null },
        )
        return
    }

    Box(Modifier.fillMaxSize().background(bgGray)) {
        Column(Modifier.fillMaxSize()) {

            // ── Tab row ───────────────────────────────────────────────────────
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = green800,
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick  = { selectedTab = 0 },
                    text = {
                        Text(
                            s.blogTabPosts,
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick  = { selectedTab = 1 },
                    text = {
                        Text(
                            s.blogTabLeaderboard,
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                )
            }

            when (selectedTab) {
                0 -> BlogListTab(
                    accessToken  = token,
                    refreshKey   = listRefreshKey,
                    onSelectBlog = { selectedBlogId = it },
                )
                1 -> LeaderboardTab(accessToken = token)
            }
        }

        // FAB
        if (selectedTab == 0) {
            ExtendedFloatingActionButton(
                onClick = { showCreate = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 24.dp)
                    .navigationBarsPadding(),
                containerColor = green800,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Text("+", fontSize = 20.sp, fontWeight = FontWeight.Light) },
                text = { Text("New Post", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
            )
        }
    }
}

// ── Blog list tab ─────────────────────────────────────────────────────────────

@Composable
private fun BlogListTab(
    accessToken: String,
    refreshKey: Int,
    onSelectBlog: (String) -> Unit,
) {
    val s     = LocalAppStrings.current
    val scope = rememberCoroutineScope()

    var posts       by remember { mutableStateOf<List<BlogDto>>(emptyList()) }
    var isLoading   by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var error       by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var currentPage by remember { mutableIntStateOf(1) }
    var totalPages  by remember { mutableIntStateOf(1) }

    fun load(page: Int, query: String, append: Boolean = false) {
        scope.launch {
            if (append) isLoadingMore = true else isLoading = true
            error = null
            try {
                val result = getBlogs(page = page, limit = 10, search = query.ifBlank { null }, accessToken = accessToken)
                posts = if (append) posts + result.data else result.data
                currentPage = result.pagination.page
                totalPages  = result.pagination.totalPages
            } catch (e: Throwable) {
                error = e.message ?: s.blogErrorLoad
            }
            isLoading = false
            isLoadingMore = false
        }
    }

    LaunchedEffect(refreshKey) { load(1, "") }

    var searchJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    Column(Modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { q ->
                searchQuery = q
                searchJob?.cancel()
                searchJob = scope.launch {
                    kotlinx.coroutines.delay(400)
                    currentPage = 1
                    load(1, q)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            placeholder = { Text(s.blogSearchHint, fontSize = 14.sp, color = gray400) },
            leadingIcon = { Text("🔍", fontSize = 16.sp) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = green800,
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedContainerColor   = Color.White,
                unfocusedContainerColor = Color.White,
            ),
        )

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = green800)
            }

            error != null -> Box(
                Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("😕", fontSize = 40.sp)
                    Text(error!!, fontSize = 14.sp, color = gray500, textAlign = TextAlign.Center)
                    Button(
                        onClick = { load(1, searchQuery) },
                        colors  = ButtonDefaults.buttonColors(containerColor = green800),
                        shape   = RoundedCornerShape(10.dp),
                    ) { Text(s.blogRetry) }
                }
            }

            posts.isEmpty() -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("📰", fontSize = 48.sp)
                    Text(s.blogEmpty, fontSize = 14.sp, color = gray500)
                }
            }

            else -> LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(posts, key = { it.id }) { post ->
                    BlogCard(post = post, onClick = { onSelectBlog(post.id) })
                }

                if (currentPage < totalPages) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isLoadingMore) {
                                CircularProgressIndicator(color = green800, modifier = Modifier.size(24.dp))
                            } else {
                                OutlinedButton(
                                    onClick = { load(currentPage + 1, searchQuery, append = true) },
                                    shape   = RoundedCornerShape(10.dp),
                                    colors  = ButtonDefaults.outlinedButtonColors(contentColor = green800),
                                ) {
                                    Text(s.blogLoadMore)
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.navigationBarsPadding()) }
            }
        }
    }
}

// ── Blog card ─────────────────────────────────────────────────────────────────

@Composable
private fun BlogCard(post: BlogDto, onClick: () -> Unit) {
    val initials = authorInitials(
        post.author?.fullName ?: "",
        post.author?.username ?: "?",
    )
    val readMin = estimateReadMinutes(post.content)

    Card(
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column {
            // Coloured top accent bar
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(listOf(green700, green600))
                    )
            )

            Column(
                Modifier.padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Title
                Text(
                    post.title,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF111827),
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 22.sp,
                )

                // Excerpt
                if (!post.content.isNullOrBlank()) {
                    Text(
                        post.content.take(120).let { if (post.content.length > 120) "$it…" else it },
                        fontSize  = 13.sp,
                        color     = gray500,
                        maxLines  = 2,
                        overflow  = TextOverflow.Ellipsis,
                        lineHeight = 19.sp,
                    )
                }

                // Tags
                if (post.tags.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        post.tags.take(3).forEachIndexed { i, tag ->
                            val (fg, bg) = tagPalette(i)
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(bg)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(tag, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = fg)
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)

                // Footer: author + stats
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // Author avatar
                        Box(
                            Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(green100),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(initials, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = green800)
                        }
                        Column {
                            Text(
                                post.author?.fullName ?: post.author?.username ?: "",
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = gray700,
                            )
                            Text(
                                formatDateShort(post.createdAt),
                                fontSize = 10.sp,
                                color    = gray400,
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // Read time
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("🕐", fontSize = 10.sp)
                            Text("${readMin}m", fontSize = 10.sp, color = gray400)
                        }
                        // Likes
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(if (post.liked) "♥" else "♡", fontSize = 12.sp, color = if (post.liked) Color(0xFFE53935) else gray400)
                            Text("${post.likeCount}", fontSize = 11.sp, color = gray400)
                        }
                    }
                }
            }
        }
    }
}

// ── Leaderboard tab ───────────────────────────────────────────────────────────

@Composable
private fun LeaderboardTab(accessToken: String) {
    val s     = LocalAppStrings.current
    val scope = rememberCoroutineScope()

    var entries   by remember { mutableStateOf<List<LeaderboardEntryDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error     by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            isLoading = true; error = null
            try { entries = getLeaderboard(accessToken).data }
            catch (e: Throwable) { error = e.message ?: s.blogErrorLoad }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    Column(Modifier.fillMaxSize()) {
        // Section header
        Box(
            Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(s.leaderboardTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = gray700)
                Text(s.leaderboardSubtitle, fontSize = 12.sp, color = gray400)
            }
        }

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = green800)
            }

            error != null -> Box(
                Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(error!!, fontSize = 14.sp, color = gray500, textAlign = TextAlign.Center)
                    Button(
                        onClick = { load() },
                        colors  = ButtonDefaults.buttonColors(containerColor = green800),
                        shape   = RoundedCornerShape(10.dp),
                    ) { Text(s.blogRetry) }
                }
            }

            entries.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("🏆", fontSize = 40.sp)
                    Text(s.leaderboardEmpty, fontSize = 14.sp, color = gray500)
                }
            }

            else -> LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Podium for top 3
                val top3 = entries.take(3)
                if (top3.size == 3) {
                    item { PodiumRow(top3) }
                    item { Spacer(Modifier.height(4.dp)) }
                }

                val rest = if (entries.size > 3) entries.drop(3) else entries
                items(rest, key = { it.userId }) { entry ->
                    LeaderboardRow(entry = entry)
                }

                item { Spacer(Modifier.navigationBarsPadding()) }
            }
        }
    }
}

// ── Podium ────────────────────────────────────────────────────────────────────

@Composable
private fun PodiumRow(top3: List<LeaderboardEntryDto>) {
    // Order: 2nd | 1st | 3rd
    val order = listOf(top3[1], top3[0], top3[2])
    val heights = listOf(72.dp, 96.dp, 56.dp)
    val medals  = listOf("🥈", "🥇", "🥉")
    val bgColors = listOf(Color(0xFFB0BEC5), Color(0xFFFFD700), Color(0xFFBF8970))

    Card(
        Modifier.fillMaxWidth(),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(top = 16.dp, bottom = 12.dp, start = 12.dp, end = 12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom,
            ) {
                order.forEachIndexed { i, entry ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(medals[i], fontSize = 28.sp)
                        // Avatar
                        val initials = authorInitials(entry.fullName, entry.username)
                        Box(
                            Modifier.size(44.dp).clip(CircleShape).background(bgColors[i]),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(initials, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Text(
                            entry.fullName.split(" ").firstOrNull() ?: entry.username,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = gray700,
                        )
                        // Podium block
                        Box(
                            Modifier.width(80.dp).height(heights[i])
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(bgColors[i].copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("#${entry.rank}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = bgColors[i])
                                Text(
                                    "${entry.reportCount} rpts",
                                    fontSize = 10.sp,
                                    color = gray500,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Leaderboard row (rank 4+) ─────────────────────────────────────────────────

@Composable
private fun LeaderboardRow(entry: LeaderboardEntryDto) {
    val s = LocalAppStrings.current

    Card(
        Modifier.fillMaxWidth(),
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Rank badge
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(gray100),
                contentAlignment = Alignment.Center,
            ) {
                Text("#${entry.rank}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = gray500)
            }

            // Author avatar
            val initials = authorInitials(entry.fullName, entry.username)
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(green100),
                contentAlignment = Alignment.Center,
            ) {
                Text(initials, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = green800)
            }

            Column(Modifier.weight(1f)) {
                Text(entry.fullName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = gray700)
                Text("@${entry.username}", fontSize = 11.sp, color = gray400)
            }

            Box(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(green50)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    s.leaderboardReports(entry.reportCount),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = green800,
                )
            }
        }
    }
}
