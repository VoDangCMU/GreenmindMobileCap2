package com.vodang.greenmind.preappsurvey

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.auth.ApiException
import com.vodang.greenmind.api.preappsurvey.PreAppSurveyAnswers
import com.vodang.greenmind.api.preappsurvey.SubmitPreAppSurveyRequest
import com.vodang.greenmind.api.preappsurvey.submitPreAppSurvey
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.time.nowIso8601
import kotlinx.coroutines.launch

// ── Design tokens ─────────────────────────────────────────────────────────────
private val green800 = Color(0xFF2E7D32)
private val green600 = Color(0xFF388E3C)
private val green400 = Color(0xFF66BB6A)
private val green50  = Color(0xFFE8F5E9)
private val green100 = Color(0xFFC8E6C9)
private val surfaceColor = Color(0xFFF9FBF9)

// ── Data ──────────────────────────────────────────────────────────────────────

private data class SurveyOption(val value: String, val label: String, val emoji: String)
private data class SurveyQuestion(
    val key: String,
    val emoji: String,
    val questionEn: String,
    val questionVi: String,
    val options: List<SurveyOption> = emptyList(),
    val isNumericInput: Boolean = false,
    val inputHintEn: String = "",
    val inputHintVi: String = "",
    val inputSuffixEn: String = "",
    val inputSuffixVi: String = "",
)

private val questions = listOf(
    // daily_spending: free numeric input (VND, > 0)
    SurveyQuestion(
        key = "daily_spending",
        emoji = "💸",
        questionEn = "How much do you typically spend per day on consumer purchases?",
        questionVi = "Bạn thường chi tiêu bao nhiêu mỗi ngày cho mua sắm tiêu dùng?",
        isNumericInput = true,
        inputHintEn = "e.g. 150000",
        inputHintVi = "ví dụ: 150000",
        inputSuffixEn = "VND / day",
        inputSuffixVi = "đ / ngày",
    ),
    // spending_variation: 1–5 scale
    SurveyQuestion(
        key = "spending_variation",
        emoji = "📊",
        questionEn = "How much does your spending vary from week to week?",
        questionVi = "Chi tiêu của bạn thay đổi bao nhiêu giữa các tuần?",
        options = listOf(
            SurveyOption("1", "Very stable",       "🧊"),
            SurveyOption("2", "Mostly stable",     "😊"),
            SurveyOption("3", "Somewhat varied",   "🤔"),
            SurveyOption("4", "Quite varied",      "🌊"),
            SurveyOption("5", "Very unpredictable","🎲"),
        )
    ),
    // brand_trial: 1–5 scale
    SurveyQuestion(
        key = "brand_trial",
        emoji = "🛍️",
        questionEn = "How often do you try new brands or products?",
        questionVi = "Bạn có thường xuyên thử thương hiệu hoặc sản phẩm mới không?",
        options = listOf(
            SurveyOption("1", "Never",     "🚫"),
            SurveyOption("2", "Rarely",    "😴"),
            SurveyOption("3", "Sometimes", "🙂"),
            SurveyOption("4", "Often",     "😄"),
            SurveyOption("5", "Always",    "🤩"),
        )
    ),
    // shopping_list: 1–5 scale
    SurveyQuestion(
        key = "shopping_list",
        emoji = "📋",
        questionEn = "Do you usually shop with a planned list?",
        questionVi = "Bạn có thường mua sắm theo danh sách đã lên kế hoạch không?",
        options = listOf(
            SurveyOption("1", "Never",     "🚫"),
            SurveyOption("2", "Rarely",    "😅"),
            SurveyOption("3", "Sometimes", "🙂"),
            SurveyOption("4", "Usually",   "✅"),
            SurveyOption("5", "Always",    "📝"),
        )
    ),
    // daily_distance: free numeric input (km, > 0)
    SurveyQuestion(
        key = "daily_distance",
        emoji = "🚶",
        questionEn = "How far do you typically travel on a normal day?",
        questionVi = "Bạn thường di chuyển bao xa trong một ngày bình thường?",
        isNumericInput = true,
        inputHintEn = "e.g. 5",
        inputHintVi = "ví dụ: 5",
        inputSuffixEn = "km / day",
        inputSuffixVi = "km / ngày",
    ),
    // new_places: 1–5 scale
    SurveyQuestion(
        key = "new_places",
        emoji = "🗺️",
        questionEn = "How often do you visit new or unfamiliar places?",
        questionVi = "Bạn có thường xuyên ghé thăm những nơi mới không?",
        options = listOf(
            SurveyOption("1", "Never",       "🏠"),
            SurveyOption("2", "Rarely",      "😴"),
            SurveyOption("3", "Sometimes",   "🙂"),
            SurveyOption("4", "Often",       "🗺️"),
            SurveyOption("5", "Very often",  "✈️"),
        )
    ),
    // public_transport: 1–5 scale
    SurveyQuestion(
        key = "public_transport",
        emoji = "🚌",
        questionEn = "How often do you use public transport?",
        questionVi = "Bạn có thường xuyên sử dụng phương tiện công cộng không?",
        options = listOf(
            SurveyOption("1", "Never",     "🚫"),
            SurveyOption("2", "Rarely",    "😴"),
            SurveyOption("3", "Sometimes", "🙂"),
            SurveyOption("4", "Often",     "🚌"),
            SurveyOption("5", "Always",    "🌍"),
        )
    ),
    // stable_schedule: 1–5 scale
    SurveyQuestion(
        key = "stable_schedule",
        emoji = "🗓️",
        questionEn = "How stable and predictable is your daily routine?",
        questionVi = "Lịch trình hàng ngày của bạn có ổn định và dễ đoán không?",
        options = listOf(
            SurveyOption("1", "Very unpredictable",   "🎲"),
            SurveyOption("2", "Mostly unpredictable", "🌊"),
            SurveyOption("3", "Mixed",                "🤔"),
            SurveyOption("4", "Mostly stable",        "😊"),
            SurveyOption("5", "Very stable",          "⏰"),
        )
    ),
    // night_outings: 1–7 (nights per week)
    SurveyQuestion(
        key = "night_outings",
        emoji = "🌙",
        questionEn = "How many nights per week do you go out in the evening?",
        questionVi = "Bạn ra ngoài vào buổi tối mấy đêm mỗi tuần?",
        options = listOf(
            SurveyOption("1", "1 night / week",  "🏠"),
            SurveyOption("2", "2 nights / week", "😴"),
            SurveyOption("3", "3 nights / week", "🌆"),
            SurveyOption("4", "4 nights / week", "🌙"),
            SurveyOption("5", "5 nights / week", "🦉"),
            SurveyOption("6", "6 nights / week", "🌃"),
            SurveyOption("7", "Every night",     "🎉"),
        )
    ),
    // healthy_eating: 1–5 scale
    SurveyQuestion(
        key = "healthy_eating",
        emoji = "🥗",
        questionEn = "How would you rate your overall eating habits?",
        questionVi = "Bạn đánh giá thói quen ăn uống của mình thế nào?",
        options = listOf(
            SurveyOption("1", "Very unhealthy",   "🍔"),
            SurveyOption("2", "Mostly unhealthy", "😅"),
            SurveyOption("3", "Mixed",            "🤔"),
            SurveyOption("4", "Mostly healthy",   "🥗"),
            SurveyOption("5", "Very healthy",     "🌿"),
        )
    ),
    // social_media: 1–5 scale
    SurveyQuestion(
        key = "social_media",
        emoji = "📱",
        questionEn = "How much time do you spend on social media each day?",
        questionVi = "Bạn dành bao nhiêu thời gian cho mạng xã hội mỗi ngày?",
        options = listOf(
            SurveyOption("1", "< 30 min",       "⚡"),
            SurveyOption("2", "30 min – 1 hr",  "🙂"),
            SurveyOption("3", "1 – 2 hrs",      "😐"),
            SurveyOption("4", "2 – 4 hrs",      "😅"),
            SurveyOption("5", "> 4 hrs",        "📱"),
        )
    ),
    // goal_setting: 1–5 scale
    SurveyQuestion(
        key = "goal_setting",
        emoji = "🎯",
        questionEn = "How often do you set and actively track personal goals?",
        questionVi = "Bạn có thường xuyên đặt và theo dõi mục tiêu cá nhân không?",
        options = listOf(
            SurveyOption("1", "Never",     "🚫"),
            SurveyOption("2", "Rarely",    "😴"),
            SurveyOption("3", "Sometimes", "🙂"),
            SurveyOption("4", "Often",     "🎯"),
            SurveyOption("5", "Always",    "🏆"),
        )
    ),
    // mood_swings: 1–5 scale
    SurveyQuestion(
        key = "mood_swings",
        emoji = "🎭",
        questionEn = "How often do you experience significant mood swings?",
        questionVi = "Bạn có thường xuyên trải qua những thay đổi tâm trạng đáng kể không?",
        options = listOf(
            SurveyOption("1", "Never",      "😌"),
            SurveyOption("2", "Rarely",     "🙂"),
            SurveyOption("3", "Sometimes",  "😐"),
            SurveyOption("4", "Often",      "😟"),
            SurveyOption("5", "Very often", "🎭"),
        )
    ),
)

// ── Main screen ───────────────────────────────────────────────────────────────

@Composable
fun PreAppSurveyScreen(onCompleted: () -> Unit = {}) {
    val s = LocalAppStrings.current
    val isVi = s.langCode == "vi"
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var currentStep by remember { mutableStateOf(0) }
    val answers = remember { mutableStateMapOf<String, String>() }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isDone by remember { mutableStateOf(false) }

    val total = questions.size

    if (isDone) {
        DoneScreen(s = s, onContinue = onCompleted)
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(surfaceColor)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Header ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(green800, green600))
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Text(
                        text = s.preAppSurveyTitle,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = s.preAppSurveyStep(currentStep + 1, total),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                    )
                    Spacer(Modifier.height(12.dp))
                    // Progress bar
                    LinearProgressIndicator(
                        progress = { (currentStep + 1).toFloat() / total },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = green400,
                        trackColor = Color.White.copy(alpha = 0.3f),
                    )
                }
            }

            // ── Question card ─────────────────────────────────────────────────
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    val forward = targetState > initialState
                    (slideInHorizontally(tween(300)) { if (forward) it else -it } + fadeIn(tween(300))) togetherWith
                        (slideOutHorizontally(tween(300)) { if (forward) -it else it } + fadeOut(tween(300)))
                },
                modifier = Modifier.weight(1f),
                label = "QuestionSlide"
            ) { step ->
                val q = questions[step]
                val selected = answers[q.key]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Spacer(Modifier.height(4.dp))
                    // Emoji + question text
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = green50),
                        elevation = CardDefaults.cardElevation(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(q.emoji, fontSize = 32.sp)
                            Text(
                                text = if (isVi) q.questionVi else q.questionEn,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = green800,
                                lineHeight = 22.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (q.isNumericInput) {
                        // Free numeric text input
                        OutlinedTextField(
                            value = selected ?: "",
                            onValueChange = { v ->
                                // only allow digits
                                if (v.all { it.isDigit() }) answers[q.key] = v
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    if (isVi) q.inputHintVi else q.inputHintEn,
                                    color = Color.Gray
                                )
                            },
                            suffix = {
                                Text(
                                    if (isVi) q.inputSuffixVi else q.inputSuffixEn,
                                    color = Color.Gray,
                                    fontSize = 13.sp,
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = green800,
                                unfocusedBorderColor = Color(0xFFBDBDBD),
                                focusedLabelColor = green800,
                            ),
                        )
                    } else {
                        // Option selection rows
                        q.options.forEach { opt ->
                            val isSelected = selected == opt.value
                            OptionRow(
                                emoji = opt.emoji,
                                label = if (isVi) viOptionLabel(q.key, opt.value) else opt.label,
                                isSelected = isSelected,
                                onClick = { answers[q.key] = opt.value }
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }

            // ── Error msg ─────────────────────────────────────────────────────
            if (errorMsg != null) {
                Text(
                    text = errorMsg!!,
                    color = Color(0xFFC62828),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // ── Navigation buttons ────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentStep > 0) {
                    OutlinedButton(
                        onClick = { currentStep--; errorMsg = null },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = green800),
                    ) {
                        Text(s.preAppSurveyPrev)
                    }
                }

                val isLast = currentStep == total - 1
                val currentQ = questions[currentStep]
                val currentKey = currentQ.key
                val rawAnswer = answers[currentKey]
                val hasAnswer = if (currentQ.isNumericInput)
                    rawAnswer != null && rawAnswer.isNotBlank() && (rawAnswer.toLongOrNull() ?: 0L) > 0L
                else
                    rawAnswer != null

                Button(
                    onClick = {
                        if (!hasAnswer) {
                            errorMsg = if (isVi) "Vui lòng chọn một câu trả lời" else "Please select an answer"
                            return@Button
                        }
                        errorMsg = null
                        if (isLast) {
                            scope.launch {
                                isSubmitting = true
                                try {
                                    val token = SettingsStore.getAccessToken() ?: ""
                                    val userId = SettingsStore.getUser()?.id ?: ""
                                    val ans = PreAppSurveyAnswers(
                                        daily_spending     = answers["daily_spending"]     ?: "50000",
                                        spending_variation = answers["spending_variation"] ?: "3",
                                        brand_trial        = answers["brand_trial"]        ?: "3",
                                        shopping_list      = answers["shopping_list"]      ?: "3",
                                        daily_distance     = answers["daily_distance"]     ?: "3",
                                        new_places         = answers["new_places"]         ?: "3",
                                        public_transport   = answers["public_transport"]   ?: "3",
                                        stable_schedule    = answers["stable_schedule"]    ?: "3",
                                        night_outings      = answers["night_outings"]      ?: "1",
                                        healthy_eating     = answers["healthy_eating"]     ?: "3",
                                        social_media       = answers["social_media"]       ?: "3",
                                        goal_setting       = answers["goal_setting"]       ?: "3",
                                        mood_swings        = answers["mood_swings"]        ?: "3",
                                    )
                                    submitPreAppSurvey(
                                        accessToken = token,
                                        request = SubmitPreAppSurveyRequest(
                                            userId = userId,
                                            answers = ans,
                                            isCompleted = true,
                                            completedAt = nowIso8601(),
                                        )
                                    )
                                    isDone = true
                                } catch (e: ApiException) {
                                    errorMsg = e.message.ifBlank { if (isVi) "Lỗi gửi khảo sát" else "Failed to submit" }
                                } catch (e: Throwable) {
                                    errorMsg = if (isVi) "Lỗi mạng. Vui lòng thử lại." else "Network error. Please try again."
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        } else {
                            currentStep++
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasAnswer) green800 else green100,
                        contentColor = if (hasAnswer) Color.White else green600,
                    ),
                    enabled = !isSubmitting,
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(if (isLast) s.preAppSurveySubmit else s.preAppSurveyNext, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ── Option row ────────────────────────────────────────────────────────────────

@Composable
private fun OptionRow(
    emoji: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) green800 else Color.White
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 2.dp else 0.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(emoji, fontSize = 22.sp)
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color(0xFF1B1B1B),
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", color = green800, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── Done screen ───────────────────────────────────────────────────────────────

@Composable
private fun DoneScreen(s: com.vodang.greenmind.i18n.AppStrings, onContinue: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(green800, green600))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("🌿", fontSize = 72.sp)
            Spacer(Modifier.height(24.dp))
            Text(
                text = s.preAppSurveyDone,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = s.preAppSurveyDoneDesc,
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
            )
            Spacer(Modifier.height(36.dp))
            Button(
                onClick = onContinue,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = green800,
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (s.langCode == "vi") "Tiếp tục" else "Continue",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

// ── Vietnamese option labels ──────────────────────────────────────────────────

private fun viOptionLabel(key: String, value: String): String = when (key) {
    "spending_variation" -> when (value) {
        "1" -> "Rất ổn định"; "2" -> "Khá ổn định"; "3" -> "Hơi thay đổi"; "4" -> "Khá thay đổi"; else -> "Rất thất thường"
    }
    "brand_trial" -> when (value) {
        "1" -> "Không bao giờ"; "2" -> "Hiếm khi"; "3" -> "Đôi khi"; "4" -> "Thường xuyên"; else -> "Luôn luôn"
    }
    "shopping_list" -> when (value) {
        "1" -> "Không bao giờ"; "2" -> "Hiếm khi"; "3" -> "Đôi khi"; "4" -> "Thường"; else -> "Luôn luôn"
    }
    "new_places" -> when (value) {
        "1" -> "Không bao giờ"; "2" -> "Hiếm khi"; "3" -> "Đôi khi"; "4" -> "Thường xuyên"; else -> "Rất thường xuyên"
    }
    "public_transport" -> when (value) {
        "1" -> "Không bao giờ"; "2" -> "Hiếm khi"; "3" -> "Đôi khi"; "4" -> "Thường xuyên"; else -> "Luôn luôn"
    }
    "stable_schedule" -> when (value) {
        "1" -> "Rất thất thường"; "2" -> "Khá thất thường"; "3" -> "Hỗn hợp"; "4" -> "Khá ổn định"; else -> "Rất ổn định"
    }
    "night_outings" -> when (value) {
        "1" -> "1 đêm / tuần"; "2" -> "2 đêm / tuần"; "3" -> "3 đêm / tuần"
        "4" -> "4 đêm / tuần"; "5" -> "5 đêm / tuần"; "6" -> "6 đêm / tuần"
        else -> "Mỗi đêm"
    }
    "healthy_eating" -> when (value) {
        "1" -> "Rất không lành mạnh"; "2" -> "Không lành mạnh"; "3" -> "Hỗn hợp"; "4" -> "Khá lành mạnh"; else -> "Rất lành mạnh"
    }
    "social_media" -> when (value) {
        "1" -> "< 30 phút"; "2" -> "30 phút – 1 tiếng"; "3" -> "1 – 2 tiếng"; "4" -> "2 – 4 tiếng"; else -> "> 4 tiếng"
    }
    "goal_setting" -> when (value) {
        "1" -> "Không bao giờ"; "2" -> "Hiếm khi"; "3" -> "Đôi khi"; "4" -> "Thường xuyên"; else -> "Luôn luôn"
    }
    "mood_swings" -> when (value) {
        "1" -> "Không bao giờ"; "2" -> "Hiếm khi"; "3" -> "Đôi khi"; "4" -> "Thường xuyên"; else -> "Rất thường xuyên"
    }
    else -> value
}
