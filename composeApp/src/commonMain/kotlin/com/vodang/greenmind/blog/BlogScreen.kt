package com.vodang.greenmind.blog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.EmojiEvents
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
import androidx.compose.foundation.layout.navigationBarsPadding
import com.vodang.greenmind.api.blog.BlogDto
import com.vodang.greenmind.api.blog.getBlogs
import com.vodang.greenmind.api.blog.getLeaderboard
import com.vodang.greenmind.api.blog.toggleBlogLike
import com.vodang.greenmind.api.campaign.CampaignDto
import com.vodang.greenmind.api.campaign.getAllCampaigns
import com.vodang.greenmind.blog.components.BlogCard
import com.vodang.greenmind.blog.components.LeaderboardTab
import com.vodang.greenmind.home.components.CampaignDetailScreen
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.platform.BackHandler
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.theme.Green100
import com.vodang.greenmind.theme.Green800
import com.vodang.greenmind.theme.Gray100
import com.vodang.greenmind.theme.Gray400
import com.vodang.greenmind.theme.Gray500
import com.vodang.greenmind.theme.Gray600
import com.vodang.greenmind.theme.Gray700
import kotlinx.coroutines.launch

private val green800 = Green800
private val green100 = Green100
private val gray100 = Gray100
private val gray400 = Gray400
private val gray500 = Gray500
private val gray600 = Gray600
private val gray700 = Gray700

private fun authorInitials(name: String, username: String): String =
    name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
        .ifBlank { username.take(2).uppercase() }

@Composable
fun BlogScreen() {
    val s = LocalAppStrings.current
    val accessToken = SettingsStore.getAccessToken() ?: return

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedBlogId by remember { mutableStateOf<String?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var listRefreshKey by remember { mutableIntStateOf(0) }
    var likedPosts by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    var likeCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var reloadTrigger by remember { mutableIntStateOf(0) }

    BackHandler(enabled = showCreate) { showCreate = false }
    BackHandler(enabled = selectedBlogId != null) { selectedBlogId = null }

    var allCampaigns by remember { mutableStateOf<List<CampaignDto>>(emptyList()) }
    var selectedCampaignId by remember { mutableStateOf<String?>(null) }

    val selectedCampaign = selectedCampaignId?.let { id -> allCampaigns.find { it.id == id } }
    if (selectedCampaign != null) {
        CampaignDetailScreen(
            campaign = selectedCampaign,
            accessToken = accessToken,
            onBack = { selectedCampaignId = null },
        )
        return
    }

    if (showCreate) {
        CreateBlogScreen(
            accessToken = accessToken,
            onBack = { showCreate = false },
            onCreated = { showCreate = false; listRefreshKey++; likedPosts = emptyMap(); likeCounts = emptyMap() },
        )
        return
    }

    val blogId = selectedBlogId
    if (blogId != null) {
        BlogDetailScreen(
            blogId = blogId,
            accessToken = accessToken,
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

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.White),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                s.blogTabPosts,
                fontSize = 14.sp,
                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedTab == 0) green800 else gray500,
                modifier = Modifier.clickable { selectedTab = 0 }.padding(vertical = 8.dp)
            )
            Text(
                s.blogTabLeaderboard,
                fontSize = 14.sp,
                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedTab == 1) green800 else gray500,
                modifier = Modifier.clickable { selectedTab = 1 }.padding(vertical = 8.dp)
            )
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFFEEF0F3))) {
            when (selectedTab) {
                0 -> BlogListTab(
                    accessToken = accessToken,
                    refreshKey = listRefreshKey,
                    reloadTrigger = reloadTrigger,
                    onSelectBlog = { selectedBlogId = it },
                    onCreatePost = { showCreate = true },
                    likedPosts = likedPosts,
                    likeCounts = likeCounts,
                    allCampaigns = allCampaigns,
                    onSelectCampaign = { selectedCampaignId = it },
                    onCampaignsLoaded = { allCampaigns = it },
                )
                1 -> LeaderboardTab(
                    accessToken = accessToken,
                    onLoad = { getLeaderboard(accessToken).data },
                )
            }
        }
    }
}

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

@Composable
private fun BlogListTab(
    accessToken: String,
    refreshKey: Int,
    reloadTrigger: Int,
    onSelectBlog: (String) -> Unit,
    onCreatePost: () -> Unit,
    likedPosts: Map<String, Boolean>,
    likeCounts: Map<String, Int>,
    allCampaigns: List<CampaignDto>,
    onSelectCampaign: (String) -> Unit,
    onCampaignsLoaded: (List<CampaignDto>) -> Unit,
) {
    val s = LocalAppStrings.current
    val scope = rememberCoroutineScope()

    var posts by remember { mutableStateOf<List<BlogDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableIntStateOf(1) }
    var totalPages by remember { mutableIntStateOf(1) }

    LaunchedEffect(accessToken) {
        try {
            val campaigns = getAllCampaigns(accessToken)
            onCampaignsLoaded(campaigns)
        } catch (_: Throwable) { }
    }

    fun load(page: Int, query: String, append: Boolean = false) {
        scope.launch {
            if (append) isLoadingMore = true else isLoading = true
            error = null
            try {
                val result = getBlogs(page = page, limit = 10, search = query.ifBlank { null }, accessToken = accessToken)
                posts = if (append) posts + result.data else result.data
                currentPage = result.pagination.page
                totalPages = result.pagination.totalPages
            } catch (e: Throwable) {
                error = e.message ?: s.blogErrorLoad
            }
            isLoading = false
            isLoadingMore = false
        }
    }

    LaunchedEffect(refreshKey) { load(1, "") }
    LaunchedEffect(reloadTrigger) { load(1, "") }

    fun toggleLike(postId: String) {
        val post = posts.find { it.id == postId } ?: return
        val effectiveLiked = likedPosts[postId] ?: post.isLiked || post.liked
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
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("😕", fontSize = 40.sp)
                Text(error!!, fontSize = 14.sp, color = gray500, textAlign = TextAlign.Center)
                Button(
                    onClick = { load(1, "") },
                    colors = ButtonDefaults.buttonColors(containerColor = green800),
                    shape = RoundedCornerShape(10.dp),
                ) { Text(s.blogRetry) }
            }
        }
        posts.isEmpty() -> Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("📰", fontSize = 48.sp)
                Text(s.blogEmpty, fontSize = 14.sp, color = gray500)
            }
        }
        else -> {
            val feedItems = buildList {
                val campaigns = allCampaigns.takeIf { it.isNotEmpty() } ?: emptyList()
                var campaignIdx = 0
                posts.forEachIndexed { index, post ->
                    add(FeedItem.Blog(post))
                    if ((index + 1) % 2 == 0 && campaignIdx < campaigns.size) {
                        add(FeedItem.Campaign(campaigns[campaignIdx]))
                        campaignIdx++
                    }
                }
            }

            LazyColumn(Modifier.fillMaxSize()) {
                item {
                    CreatePostPrompt(onTap = onCreatePost)
                    Spacer(Modifier.height(8.dp))
                }
                items(feedItems, key = {
                    when (it) {
                        is FeedItem.Blog -> "blog-${it.post.id}"
                        is FeedItem.Campaign -> "campaign-${it.campaign.id}"
                    }
                }) { item ->
                    when (item) {
                        is FeedItem.Blog -> {
                            val effectiveLiked = likedPosts[item.post.id] ?: item.post.isLiked || item.post.liked
                            val effectiveCount = likeCounts[item.post.id] ?: item.post.likeCount
                            BlogCard(
                                post = item.post,
                                effectiveLiked = effectiveLiked,
                                effectiveLikeCount = effectiveCount,
                                onClick = { onSelectBlog(item.post.id) },
                                onLike = { toggleLike(item.post.id) },
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        is FeedItem.Campaign -> {
                            SponsoredCampaignCard(
                                campaign = item.campaign,
                                onClick = { onSelectCampaign(item.campaign.id) },
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
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
                                    onClick = { load(currentPage + 1, "", append = true) },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = green800),
                                ) { Text(s.blogLoadMore) }
                            }
                        }
                    }
                }
            }
        }
    }
}

private sealed class FeedItem {
    data class Blog(val post: BlogDto) : FeedItem()
    data class Campaign(val campaign: CampaignDto) : FeedItem()
}

@Composable
private fun SponsoredCampaignCard(campaign: CampaignDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🎯", fontSize = 16.sp)
                Text("Sponsored", fontSize = 12.sp, color = Gray500, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(8.dp))
            Text(campaign.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1B1B1B))
            campaign.description?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, fontSize = 13.sp, color = Gray600, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}