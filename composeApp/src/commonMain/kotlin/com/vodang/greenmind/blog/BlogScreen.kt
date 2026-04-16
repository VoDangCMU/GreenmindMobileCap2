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
import com.vodang.greenmind.api.blog.BlogDto
import com.vodang.greenmind.api.blog.LeaderboardEntryDto
import com.vodang.greenmind.api.blog.getBlogs
import com.vodang.greenmind.api.blog.getLeaderboard
import com.vodang.greenmind.api.blog.getMyBlogs
import com.vodang.greenmind.api.blog.toggleBlogLike
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.platform.BackHandler
import com.vodang.greenmind.store.SettingsStore
import kotlinx.coroutines.launch

// -- Palette (green scheme preserved) ------------------------------------------

private val green800 = Color(0xFF2E7D32)
private val green600 = Color(0xFF43A047)
private val green50  = Color(0xFFE8F5E9)
private val green100 = Color(0xFFC8E6C9)
private val feedBg   = Color(0xFFEEF0F3)
private val gray700  = Color(0xFF374151)
private val gray500  = Color(0xFF6B7280)
private val gray400  = Color(0xFF9CA3AF)
private val gray100  = Color(0xFFF3F4F6)
private val divider  = Color(0xFFE4E6EB)
private val red500   = Color(0xFFE53935)

// -- Helpers -------------------------------------------------------------------

private fun formatDateShort(iso: String): String = try {
    val parts = iso.substringBefore('T').split('-')
    if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else iso
} catch (_: Throwable) { iso }

private fun estimateReadMinutes(content: String?): Int =
    ((content?.split(" ")?.size ?: 0) / 200).coerceAtLeast(1)

private fun authorInitials(name: String, username: String): String =
    name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
        .ifBlank { username.take(2).uppercase() }

// -- Screen --------------------------------------------------------------------

@Composable
fun BlogScreen() {
    val s = LocalAppStrings.current
    val accessToken = SettingsStore.getAccessToken()

    var selectedTab    by remember { mutableIntStateOf(0) }
    var selectedBlogId by remember { mutableStateOf<String?>(null) }
    var showCreate     by remember { mutableStateOf(false) }
    var listRefreshKey by remember { mutableIntStateOf(0) }
    var likedPosts     by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    var likeCounts     by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var reloadTrigger  by remember { mutableIntStateOf(0) }

    val token = accessToken ?: return

    BackHandler(enabled = showCreate) { showCreate = false }
    BackHandler(enabled = selectedBlogId != null) { selectedBlogId = null }

    if (showCreate) {
        CreateBlogScreen(
            accessToken = token,
            onBack = { showCreate = false },
            onCreated = { showCreate = false; listRefreshKey++; likedPosts = emptyMap(); likeCounts = emptyMap() },
        )
        return
    }
    val blogId = selectedBlogId
    if (blogId != null) {
        BlogDetailScreen(
            blogId = blogId,
            accessToken = token,
            onBack = { selectedBlogId = null; reloadTrigger++ },
            onBlogLiked = { id, liked, count ->
                likedPosts = likedPosts + (id to liked)
                likeCounts = likeCounts + (id to count)
            },
            onBlogDeleted = {
                selectedBlogId = null
                likedPosts = likedPosts - blogId
                likeCounts = likeCounts - blogId
                reloadTrigger++
            },
        )
        return
    }

    Column(Modifier.fillMaxSize().background(feedBg)) {
        // -- Tab row -------------------------------------------------------
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
                reloadTrigger = reloadTrigger,
                onSelectBlog = { selectedBlogId = it },
                onCreatePost = { showCreate = true },
                likedPosts   = likedPosts,
                likeCounts   = likeCounts,
            )
            1 -> LeaderboardTab(accessToken = token)
        }
    }
}

// -- "Create post" prompt (Facebook-style) -------------------------------------

@Composable
private fun CreatePostPrompt(onTap: () -> Unit) {
    val user = SettingsStore.getUser()
    val initials = authorInitials(user?.fullName ?: "", user?.username ?: "?")
    val s = LocalAppStrings.current

    Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onTap)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(green100),
                contentAlignment = Alignment.Center,
            ) {
                Text(initials, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = green800)
            }
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(gray100)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(s.blogCreateContentHint, fontSize = 14.sp, color = gray400)
            }
        }
    }
}

// -- Blog list tab -------------------------------------------------------------

@Composable
private fun BlogListTab(
    accessToken: String,
    refreshKey: Int,
    reloadTrigger: Int,
    onSelectBlog: (String) -> Unit,
    onCreatePost: () -> Unit,
    likedPosts: Map<String, Boolean>,
    likeCounts: Map<String, Int>,
) {
    val s     = LocalAppStrings.current
    val scope = rememberCoroutineScope()

    var posts         by remember { mutableStateOf<List<BlogDto>>(emptyList()) }
    var isLoading     by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var error         by remember { mutableStateOf<String?>(null) }
    var currentPage   by remember { mutableIntStateOf(1) }
    var totalPages    by remember { mutableIntStateOf(1) }

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

    LaunchedEffect(refreshKey) {
        load(1, "")
    }

    LaunchedEffect(reloadTrigger) {
        load(1, "")
    }

    fun toggleLike(postId: String) {
        val post = posts.find { it.id == postId } ?: return
        val effectiveLiked = likedPosts[postId] ?: post.isLiked || post.liked
        // Optimistic update
        posts = posts.map {
            if (it.id == postId) it.copy(
                isLiked = !effectiveLiked,
                likeCount = (it.likeCount + if (!effectiveLiked) 1 else -1).coerceAtLeast(0),
            ) else it
        }
        scope.launch {
            try {
                toggleBlogLike(postId, accessToken)
            } catch (e: Throwable) {
                // Revert on failure
                posts = posts.map {
                    if (it.id == postId) it.copy(isLiked = effectiveLiked, likeCount = it.likeCount + if (effectiveLiked) 1 else -1)
                    else it
                }
            }
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
                Text("\uD83D\uDE15", fontSize = 40.sp)
                Text(error!!, fontSize = 14.sp, color = gray500, textAlign = TextAlign.Center)
                Button(
                    onClick = { load(1, "") },
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
                Text("\uD83D\uDCF0", fontSize = 48.sp)
                Text(s.blogEmpty, fontSize = 14.sp, color = gray500)
            }
        }

        else -> LazyColumn(Modifier.fillMaxSize()) {
            // -- "What's on your mind?" row --------------------------------
            item {
                CreatePostPrompt(onTap = onCreatePost)
                Spacer(Modifier.height(8.dp))
            }

            // -- Posts (edge-to-edge white blocks separated by gray gap) ---
            items(posts, key = { it.id }) { post ->
                val effectiveLiked = likedPosts[post.id] ?: post.isLiked || post.liked
                val effectiveCount = likeCounts[post.id] ?: post.likeCount
                BlogCard(
                    post = post,
                    effectiveLiked = effectiveLiked,
                    effectiveLikeCount = effectiveCount,
                    onClick = { onSelectBlog(post.id) },
                    onLike = { toggleLike(post.id) },
                )
                Spacer(Modifier.height(8.dp))
            }

            // -- Load more -------------------------------------------------
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
                                onClick = { load(currentPage + 1, "", append = true) },
                                shape   = RoundedCornerShape(20.dp),
                                colors  = ButtonDefaults.outlinedButtonColors(contentColor = green800),
                            ) { Text(s.blogLoadMore) }
                        }
                    }
                }
            }

            item { Spacer(Modifier.navigationBarsPadding()) }
        }
    }
}

// -- Blog card (Facebook post style) -------------------------------------------

@Composable
private fun BlogCard(
    post: BlogDto,
    effectiveLiked: Boolean,
    effectiveLikeCount: Int,
    onClick: () -> Unit,
    onLike: () -> Unit,
) {
    val s = LocalAppStrings.current
    val initials = authorInitials(
        post.author?.fullName ?: "",
        post.author?.username ?: "?",
    )
    val readMin = estimateReadMinutes(post.content)

    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = Color.White,
    ) {
        Column {
            // -- Header: avatar \u00B7 name \u00B7 date ------------------------------
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(green100),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(initials, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = green800)
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        post.author?.fullName ?: post.author?.username ?: "",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color(0xFF050505),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(formatDateShort(post.createdAt), fontSize = 12.sp, color = gray500)
                        Text("\u00B7", fontSize = 12.sp, color = gray500)
                        Text("\uD83D\uDD50 ${readMin}m", fontSize = 12.sp, color = gray500)
                    }
                }
            }

            // -- Title -----------------------------------------------------
            Text(
                post.title,
                modifier   = Modifier.padding(horizontal = 16.dp),
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                color      = Color(0xFF050505),
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis,
                lineHeight = 22.sp,
            )

            // -- Body excerpt ----------------------------------------------
            if (!post.content.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                val excerpt = if (post.content.length > 150) post.content.take(150) + "..." else post.content
                Text(
                    excerpt,
                    modifier   = Modifier.padding(horizontal = 16.dp),
                    fontSize   = 14.sp,
                    color      = Color(0xFF333333),
                    maxLines   = 4,
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 21.sp,
                )
            }

            // -- Tags as #hashtags -----------------------------------------
            if (post.tags.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Row(
                    Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    post.tags.take(4).forEach { tag ->
                        Text("#$tag", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = green800)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // -- Like count summary (e.g. "\u2665 12") -------------------------
            if (effectiveLikeCount > 0) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("\u2665", fontSize = 14.sp, color = if (effectiveLiked) red500 else gray500)
                    Text("$effectiveLikeCount", fontSize = 13.sp, color = gray500)
                }
                Spacer(Modifier.height(6.dp))
            }

            // -- Divider ---------------------------------------------------
            HorizontalDivider(Modifier.padding(horizontal = 16.dp), thickness = 1.dp, color = divider)

            // -- Action bar: Like \u00B7 Read -----------------------------------
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                TextButton(onClick = onLike, modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            if (effectiveLiked) "\u2665" else "\u2661",
                            fontSize = 18.sp,
                            color = if (effectiveLiked) red500 else gray500,
                        )
                        Text(
                            if (effectiveLiked) s.blogLiked else s.blogLike,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (effectiveLiked) red500 else gray500,
                        )
                    }
                }
                //TextButton(onClick = onClick, modifier = Modifier.weight(1f)) {
                //    Row(
                //        verticalAlignment = Alignment.CenterVertically,
                //        horizontalArrangement = Arrangement.spacedBy(6.dp),
                //    ) {
                //        Text("\uD83D\uDCD6", fontSize = 16.sp)
                //        Text(s.blogLoadMore, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = gray500)
                //    }
                //}
            }
        }
    }
}

// -- Leaderboard tab -----------------------------------------------------------

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
                    Text("\uD83C\uDFC6", fontSize = 40.sp)
                    Text(s.leaderboardEmpty, fontSize = 14.sp, color = gray500)
                }
            }

            else -> LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
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

// -- Podium --------------------------------------------------------------------

@Composable
private fun PodiumRow(top3: List<LeaderboardEntryDto>) {
    val order   = listOf(top3[1], top3[0], top3[2])
    val heights = listOf(72.dp, 96.dp, 56.dp)
    val medals  = listOf("\uD83E\uDD48", "\uD83E\uDD47", "\uD83E\uDD49")
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
                        Box(
                            Modifier.width(80.dp).height(heights[i])
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(bgColors[i].copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("#${entry.rank}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = bgColors[i])
                                Text("${entry.reportCount} rpts", fontSize = 10.sp, color = gray500)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -- Leaderboard row (rank 4+) -------------------------------------------------

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
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(gray100),
                contentAlignment = Alignment.Center,
            ) {
                Text("#${entry.rank}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = gray500)
            }

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
