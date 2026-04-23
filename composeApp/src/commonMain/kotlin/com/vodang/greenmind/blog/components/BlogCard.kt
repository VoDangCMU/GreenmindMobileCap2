package com.vodang.greenmind.blog.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.blog.BlogDto
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.theme.Green100
import com.vodang.greenmind.theme.Green800
import com.vodang.greenmind.theme.Gray600
import com.vodang.greenmind.theme.Gray700
import com.vodang.greenmind.theme.Gray400

private val green800 = Green800
private val green100 = Green100
private val gray600 = Gray600
private val gray700 = Gray700
private val gray400 = Gray400
private val red500 = Color(0xFFE53935)
private val divider = Color(0xFFE4E6EB)

private fun formatDateShort(iso: String): String = try {
    val parts = iso.substringBefore('T').split('-')
    if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else iso
} catch (_: Throwable) { iso }

private fun estimateReadMinutes(content: String?): Int =
    ((content?.split(" ")?.size ?: 0) / 200).coerceAtLeast(1)

private fun authorInitials(name: String, username: String): String =
    name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
        .ifBlank { username.take(2).uppercase() }

@Composable
fun BlogCard(
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
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF050505),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(formatDateShort(post.createdAt), fontSize = 12.sp, color = gray600)
                        Text("·", fontSize = 12.sp, color = gray600)
                        Text("🕐 ${readMin}m", fontSize = 12.sp, color = gray600)
                    }
                }
            }

            Text(
                post.title,
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF050505),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 22.sp,
            )

            if (!post.content.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                val excerpt = if (post.content.length > 200) post.content.take(200) + "..." else post.content
                Text(
                    excerpt,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 14.sp,
                    color = Color(0xFF1A1A1A),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 21.sp,
                )
            }

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

            if (effectiveLikeCount > 0 || post.commentCount > 0) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (effectiveLikeCount > 0) {
                        Text("♥", fontSize = 14.sp, color = if (effectiveLiked) red500 else gray600)
                        Text("$effectiveLikeCount", fontSize = 13.sp, color = gray600)
                    }
                    if (effectiveLikeCount > 0 && post.commentCount > 0) {
                        Text(" · ", fontSize = 13.sp, color = gray600)
                    }
                    if (post.commentCount > 0) {
                        Text("${post.commentCount} ${s.blogComments}", fontSize = 13.sp, color = gray600)
                    }
                }
                Spacer(Modifier.height(6.dp))
            }

            HorizontalDivider(Modifier.padding(horizontal = 16.dp), thickness = 1.dp, color = divider)

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
                            if (effectiveLiked) "♥" else "♡",
                            fontSize = 18.sp,
                            color = if (effectiveLiked) red500 else gray600,
                        )
                        Text(
                            if (effectiveLiked) s.blogLiked else s.blogLike,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (effectiveLiked) red500 else gray600,
                        )
                    }
                }
                TextButton(onClick = onClick, modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("💬", fontSize = 16.sp, color = gray600)
                        Text(s.blogCommentLabel, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = gray600)
                    }
                }
            }
        }
    }
}