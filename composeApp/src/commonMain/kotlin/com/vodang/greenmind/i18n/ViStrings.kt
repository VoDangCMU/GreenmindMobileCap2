package com.vodang.greenmind.i18n

import com.vodang.greenmind.fmt

val ViStrings = AppStrings("vi").apply {


    appName = "GreenMind"
    appSubtitle = "Smart Environmental System"

    back = "QUAY LẠI"
    cancel = "Hủy"
    save = "Lưu"
    close = "Đóng"
    select = "Chọn"
    delete = "Xóa"
    today = "Hôm nay"
    or = "  HOẶC  "
    show = "Hiện"
    hide = "Ẩn"

    signIn = "Đăng nhập"
    emailAddress = "Địa chỉ email"
    emailPlaceholder = "you@example.com"
    password = "Mật khẩu"
    rememberMe = "Ghi nhớ đăng nhập"
    forgotPassword = "Quên mật khẩu?"
    loginError = "Vui lòng nhập email và mật khẩu"
    loginButton = "ĐĂNG NHẬP"
    loggingIn = "ĐANG ĐĂNG NHẬP..."
    continueWithGoogle = "Tiếp tục với Google"
    noAccount = "Chưa có tài khoản? "
    signUp = "Đăng ký"
    chooseAccount = "Chọn tài khoản"
    noSavedAccounts = "Chưa có tài khoản nào"
    addAccount = "Thêm tài khoản"

    createAccount = "Tạo tài khoản"
    stepInfo = "Thông tin"
    stepPassword = "Mật khẩu"
    stepAddress = "Địa chỉ"
    yourInformation = "Thông tin của bạn"
    setPassword = "Đặt mật khẩu"
    addressAndTerms = "Địa chỉ & Điều khoản"
    fullName = "Họ và tên"
    gender = "Giới tính"
    male = "Nam"
    female = "Nữ"
    other = "Khác"
    next = "TIẾP THEO"
    confirmPassword = "Xác nhận mật khẩu"
    passwordMismatch = "Mật khẩu không khớp"
    address = "Địa chỉ"
    road = "Đường"
    country = "Quốc gia"
    selectCountry = "Chọn quốc gia"
    searchCountry = "Tìm quốc gia..."
    city = "Thành phố"
    selectCity = "Chọn thành phố"
    searchCity = "Tìm thành phố..."
    acceptTerms = "Tôi đồng ý với điều khoản và điều kiện"
    register = "ĐĂNG KÝ"
    alreadyHaveAccount = "Đã có tài khoản? "

    home = "Trang chủ"
    todos = "Việc cần làm"
    surveys = "Khảo sát"
    blog = "Blog"
    settings = "Cài đặt"
    editProfile = "Chỉnh sửa hồ sơ"
    catalogue = "Danh mục tính năng"
    catalogueSubtitle = "Tất cả tính năng ứng dụng"
    logout = "Đăng xuất"

    greeting = { name -> "Xin chào, $name" }
    welcomeBack = "Chào mừng trở lại"

    chooseRole = "Chọn vai trò"
    householdRole = "Hộ gia đình"
    collectorRole = "Thu gom rác"
    volunteerRole = "Tình nguyện"

    language = "Ngôn ngữ"
    langVietnamese = "Tiếng Việt"
    langEnglish = "English"

    todayOverview = "Tổng quan hôm nay"
    wasteSort = "Phân loại rác"
    wasteSortValue = "3 loại"
    garbageDrop = "Báo cáo rác"
    garbageDropStatus = "Chưa đưa"
    electricityUsage = "Tiêu thụ điện"
    electricityValue = "4.2 kWh"
    greenSpending = "Chi tiêu xanh"
    greenSpendingValue = "320k đ"
    greenSpendingMonth = "Tháng 3"
    greenMealMetric = "Bữa ăn xanh"
    greenMealPercent = "68%"
    greenMealSubtitle = "Rau xanh hôm nay"
    todosMetric = "Todos"
    todosValue = { done, total -> "$done/$total xong" }
    walkDistance = "Quãng đường"
    walkValue = "4.7 km"
    features = "Chức năng"
    wasteSortDesc = "Chụp ảnh phân loại"
    garbageDropDesc = "Check-in báo cáo rác"
    wasteReport = "Báo cáo rác"
    wasteReportDesc = "Gửi & xem báo cáo rác"
    todosDesc = "Danh sách việc cần làm"
    electricityChartDesc = "Xem biểu đồ điện"
    scanMeal = "Scan món ăn"
    scanMealDesc = "Chụp ảnh bữa ăn"
    scanBill = "Quét hoá đơn"
    scanBillDesc = "Quét hoá đơn mua sắm"
    wasteImpactTitle = "Phân tích ảnh hưởng"
    greenScoreTitle = "Điểm Xanh"
    wasteImpactDesc = "Thống kê ảnh hưởng rác"
    wasteImpactTotalReports = "Tổng báo cáo"
    wasteImpactTotalKg = "Tổng kg báo cáo"
    wasteImpactResolvedRate = "Đã xử lý"
    wasteImpactStatusTitle = "Phân loại trạng thái"
    wasteImpactTypeTitle = "Theo loại rác"
    wasteImpactTimelineTitle = "Xu hướng kg theo tuần"
    wasteImpactTopWardsTitle = "Khu vực báo cáo nhiều nhất"
    wasteImpactResolved = "Đã xử lý"
    wasteImpactPending = "Chờ xử lý"
    wasteImpactNoData = "Chưa có báo cáo nào.\nGửi báo cáo để xem ảnh hưởng của bạn."
    wasteImpactLoading = "Đang tải dữ liệu…"
    wasteImpactError = "Tải thất bại. Nhấn để thử lại."
    wasteImpactRetry = "Thử lại"
    wasteImpactTip = "♻️  Mỗi báo cáo giúp người thu gom ưu tiên dọn dẹp khu vực của bạn."
    householdWasteStatusTitle = "Tình trạng rác hộ GĐ"
    householdWasteStatusDesc = "Xem tình trạng rác"
    wasteStatTitle = "Thống kê rác thải"
    wasteStatDesc = "Lượng rác chi tiết"
    scanBillMealTitle = "Scan Bill & Bữa ăn"
    scanBillMealDesc = "Quét hóa đơn và đồ ăn"
    wasteToday = "Rác hôm nay"
    wasteBagsCount = { n -> "$n kg" }
    greenerLabel = "Điểm xanh hơn"
    greenerUp = "↑ Đang xanh hơn"
    greenerDown = "↓ Cần cải thiện"
    noActivityToday = "Chưa có hoạt động"

    electricityWeekTitle = "Tiêu thụ điện tuần này"
    electricityWeekSummary = "Tổng tuần: 31.7 kWh  |  So tuần trước: ▲5%"
    weekDays = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    energyTodayLabel = "Hôm nay"
    energyMonthTotal = "Tổng tháng"
    energyAvgDaily = "Trung bình ngày"
    energyTips = "Mẹo tiết kiệm điện"
    energyTipsList = listOf(
        "💡 Dùng đèn LED — tiết kiệm 75% điện năng",
        "🌡️ Đặt điều hoà 26°C để giảm chi phí làm mát",
        "🔌 Rút phích cắm thiết bị chờ — vẫn tiêu thụ điện",
        "🪟 Tận dụng ánh sáng tự nhiên ban ngày",
        "🧺 Giặt quần áo bằng nước lạnh — tiết kiệm 90% năng lượng sưởi",
    )

    greenMealCardTitle = "Bữa ăn xanh hôm nay"
    greenMealCardDesc = "Rau củ chiếm 68% khẩu phần ăn — Tốt! 🌟"

    todosScreenTitle = "Việc cần làm"
    todosAddPlaceholder = "Việc mới..."
    todosAddSubPlaceholder = "Việc phụ mới..."
    todosEmpty = "Chưa có việc nào"

    todosCardTitle = "Việc cần làm hôm nay"
    todosCardProgress = { done, total -> "$done/$total hoàn thành" }

    oceanTitle = "Chỉ số OCEAN"
    oceanSubtitle = "Nhân cách môi trường"

    collectorTitle = "Người thu rác"
    collectorShift = "Thứ Hai, 17/03/2026 · Ca sáng · Đà Nẵng"
    collectorPointsUnit = "điểm"
    progressToday = "Tiến độ hôm nay"
    pointsCollected = { n -> "$n điểm đã lấy" }
    pointsRemaining = { n -> "$n điểm còn lại" }
    zoneLabel = "Khu vực"
    zoneValue = "3 khu"
    bagsLabel = "Khối lượng"
    bagsEstimated = "Dự kiến"
    routeLabel = "Lộ trình"
    heatmapFeatureLabel = "Heatmap rác"
    heatmapFeatureDesc = "Xem bản đồ điểm thu"
    scheduleLabel = "Lịch trình"
    scheduleDesc = "Lộ trình thu rác"

    heatmapCardTitle = "🗺️ Bản đồ nhiệt rác"
    heatLow = "Ít"
    heatHigh = "Nhiều"

    routeCardTitle = "📍 Lộ trình thu gom"
    routeMapCardTitle = "🗺️ Bản đồ lộ trình"
    routeMapLegendStart = "Bắt đầu"
    routeMapLegendStop = "Điểm dừng"
    routeMapLegendEnd = "Kết thúc"
    bagsUnit = "kg"

    checkInCardTitle = "✅ Check-in lấy rác"
    checkInButton = "Check-in tại điểm này"
    checkInDone = "Hoàn thành tất cả điểm!"
    checkInDoneDesc = "Bạn đã lấy rác tại tất cả điểm hôm nay."
    nextPointLabel = "Điểm tiếp theo"
    bagsEstimatedFmt = { n -> "$n kg dự kiến" }

    collectedLabel = "Đã lấy"
    notCollectedLabel = "Chưa lấy"

    volunteerTitle = "Tình nguyện viên"
    volunteerWip = "Tính năng đang được phát triển.\nSẽ sớm ra mắt!"
    volunteerSubtitle = "Bảo vệ môi trường · Đà Nẵng"
    volunteerHoursLabel = "Giờ tình nguyện"
    volunteerHoursValue = "24h"
    volunteerEventsLabel = "Sự kiện tham gia"
    volunteerEventsValue = "8"
    volunteerPointsLabel = "Điểm tích lũy"
    volunteerPointsValue = "1.240"
    volunteerEventsCardTitle = "🗓️ Sự kiện đang diễn ra"
    volunteerUpcomingTitle = "📅 Sự kiện sắp tới"
    volunteerJoinButton = "Đăng ký"
    volunteerRegistered = "Đã đăng ký"
    volunteerCheckIn = "Check-in"
    volunteerCheckOut = "Check-out"
    volunteerCheckedIn = "Đã check-in"
    volunteerCheckedOut = "Hoàn thành"
    volunteerLoading = "Đang tải sự kiện…"
    volunteerLoadError = "Không thể tải sự kiện"
    volunteerNoCampaigns = "Không có sự kiện nào"

    profileName = "Tên người dùng"
    profileEmail = "user@example.com"
    profileTitle = "Hồ sơ của tôi"
    todayJourney = "Hành trình hôm nay"
    walkTimeLabel = "Thời gian đi bộ"
    walkTimeMin = { n -> if (n < 60) "~${n}m" else "~${n/60}h ${n%60}m" }
    caloriesLabel = "Calo"
    caloriesKcal = { n -> "~${n} kcal" }
    personalInfo = "Thông tin cá nhân"
    genderLabel = "Giới tính"
    ageLabel = "Tuổi"
    locationLabel = "Địa điểm"
    roleLabel = "Vai trò"
    notSet = "Chưa đặt"
    ecoToday = "Hoạt động xanh hôm nay"
    mealsToday = { n -> "$n bữa ăn" }
    billsToday = { n -> "$n hoá đơn" }

    settingsLocation            = "Theo dõi vị trí"
    settingsTrackingEnabled     = "Đang theo dõi"
    settingsTrackingEnabledDesc = "Lưu vị trí GPS ở chế độ nền"
    settingsEnableRoleSwitcher = "Bật chuyển vai trò"
    settingsEnableRoleSwitcherDesc = "Hiển thị nút chuyển vai trò trên thanh trên cùng"
    settingsInterval            = "Khoảng thời gian cập nhật"
    settingsIntervalDesc        = "Tần suất lưu vị trí của bạn"
    settingsMinMove             = "Ngưỡng đứng yên"
    settingsMinMoveDesc         = { m -> "Dưới ${m}m = đứng yên" }
    settingsSpeedFilter         = "Bộ lọc tốc độ"
    settingsSpeedFilterDesc     = { v -> "Chuyển động nhanh hơn ${v.fmt(1)} m/s bị coi là đi phương tiện" }
    settingsSpeedWalk           = "Đi bộ"
    settingsSpeedRun            = "Chạy"
    settingsSpeedCycle          = "Xe đạp"
    settingsGeneral             = "Chung"
    settingsAbout               = "Thông tin"
    settingsHowWorks            = "Cách theo dõi hoạt động"
    settingsHowWorksBody        = { secs, meters, speed ->
        "GPS được lấy mẫu mỗi ${secs}s. Các điểm trong vòng ${meters}m được coi là đứng yên. " +
        "Chuyển động nhanh hơn ${speed.fmt(1)} m/s bị loại bỏ là đi phương tiện."
}
    settingsVersion             = "Phiên bản"

    surveysDesc = "Tham gia khảo sát để giúp cải thiện cộng đồng của bạn"
    blogDesc = "Đọc bài viết và mẹo về lối sống xanh"
    blogSubtitle = "Bài viết & mẹo sống xanh"
    blogEmpty = "Chưa có bài viết nào. Quay lại sau nhé!"
    blogLoading = "Đang tải bài viết..."
    blogErrorLoad = "Không thể tải bài viết"
    blogRetry = "Thử lại"
    blogBy = "bởi"
    blogLike = "Thích"
    blogLiked = "Đã thích"
    blogSearchHint = "Tìm kiếm bài viết..."
    blogLoadMore = "Tải thêm"
    blogTabPosts = "Bài viết"
    blogTabLeaderboard = "Bảng xếp hạng"
    leaderboardTitle = "Bảng xếp hạng"
    leaderboardSubtitle = "Top người đóng góp báo cáo rác"
    leaderboardReports = { n -> "$n báo cáo" }
    leaderboardEmpty = "Chưa có dữ liệu"
    blogCreate = "Bài viết mới"
    blogCreateTitle = "Tiêu đề"
    blogCreateTitleHint = "Nhập tiêu đề bài viết..."
    blogCreateContent = "Nội dung"
    blogCreateContentHint = "Viết nội dung bài viết của bạn tại đây..."
    blogCreateTags = "Thẻ"
    blogCreateTagsHint = "vd: tái chế, môi trường (phân cách bằng dấu phẩy)"
    blogCreateSubmit = "Đăng bài"
    blogCreateSubmitting = "Đang đăng..."
    blogCreateSuccess = "Đã đăng bài!"
    blogCreateErrorEmpty = "Tiêu đề và nội dung là bắt buộc"
    blogEdit = "Sửa"
    blogDelete = "Xóa"
    blogDeleteTitle = "Xóa bài viết"
    blogDeleteConfirm = "Bạn có chắc muốn xóa bài viết này? Hành động này không thể hoàn tác."
    blogEditComment = "Sửa bình luận"
    blogDeleteCommentTitle = "Xóa bình luận"
    blogDeleteCommentConfirm = "Bạn có chắc muốn xóa bình luận này?"
    blogCommentsLabel = "bình luận"
    blogCommentsTitle = { n -> "$n bình luận" }
    blogAddCommentHint = "Viết bình luận..."
    surveyMinutes = { n -> "~$n phút" }
    surveyQuestions = { n -> "$n câu hỏi" }
    surveyCompleted = "✓ Đã làm"
    surveyStart = "Bắt đầu →"
    surveyRetake = "Làm lại →"
    surveyQuestion = { cur, total -> "Câu $cur / $total" }
    surveyNext = "Tiếp theo"
    surveyPrev = "Quay lại"
    surveySubmit = "Nộp bài"
    surveySubmitted = "Cảm ơn bạn!"
    surveySubmittedDesc = "Câu trả lời của bạn đã được ghi nhận. Bạn đang góp phần làm cộng đồng xanh hơn."
    surveySelectMultiple = "Có thể chọn nhiều đáp án"
    surveyTypeAnswer = "Nhập câu trả lời của bạn..."
    surveyBackToList = "Quay lại danh sách"

    mealScreenTitle = "Bữa ăn của tôi"
    mealListEmpty = "Chưa có bữa ăn nào"
    mealScanTitle = "Quét bữa ăn"
    mealCapture = "Chụp ảnh"
    mealUpload = "Tải từ thư viện"
    mealRetake = "Chụp lại"
    mealAnalyze = "Phân tích"
    mealSave = "Lưu bữa ăn"
    mealNameLabel = "Tên bữa ăn"
    mealAnalyzing = "Đang phân tích tỉ lệ rau củ..."
    mealScanAgain = "Quét lại"
    mealPlantRatio = { pct -> "Tỉ lệ thực vật: $pct%" }
    mealRatioGood = "Tỉ lệ rau củ tuyệt vời! 🌟"
    mealRatioOk = "Cố gắng tốt 🌿"
    mealRatioLow = "Hãy thêm rau củ vào bữa ăn 🥦"
    mealPermissionNeeded = "Cần quyền truy cập camera"
    mealGrantPermission = "Cấp quyền Camera"
    mealError = "Phân tích thất bại. Thử lại."

    // ── Bill scan ─────────────────────────────────────────────────────────────────
    billScreenTitle = "Lịch sử hoá đơn"
    billListEmpty = "Chưa quét hoá đơn nào"
    billScanTitle = "Quét hoá đơn"
    billScanHint = "Hướng camera vào hoá đơn của bạn"
    billCapture = "Chụp ảnh"
    billUpload = "Tải từ thư viện"
    billRetake = "Chụp lại"
    billAnalyze = "Phân tích"
    billSave = "Lưu hoá đơn"
    billStoreNameLabel = "Tên cửa hàng"
    billAnalyzing = "Đang phân tích hoá đơn..."
    billScanAgain = "Quét lại"
    billGreenRatio = { pct -> "Chi tiêu xanh: $pct%" }
    billGreenAmount = { amt -> "Xanh: ${amt.fmt(2)}đ" }
    billRatioGood = "Mua sắm thân thiện môi trường! 🌟"
    billRatioOk = "Lựa chọn xanh tốt 🌿"
    billRatioLow = "Hãy chọn thêm sản phẩm thân thiện môi trường 🌱"
    billTotal = "Tổng cộng"
    billGreenSpend = "Chi tiêu xanh"
    billItems = "Danh sách mặt hàng"
    billPermissionNeeded = "Cần quyền truy cập camera"
    billGrantPermission = "Cấp quyền Camera"
    billError = "Phân tích thất bại. Thử lại."

    // ── Waste report scan ──────────────────────────────────────────────────────
    wasteReportLabel = "Loại rác"
    wasteReportContinue = "Tiếp tục"
    wasteReportMyTab = "Của tôi"
    wasteReportAllTab = "Tất cả"
    wasteReportEmpty = "Chưa có báo cáo nào.\nNhấn + để gửi báo cáo rác đầu tiên."
    wasteReportCapture = "Nhấn để chụp ảnh rác"
    wasteReportRetake = "Chụp lại"
    wasteReportSubmit = "Gửi báo cáo"
    wasteReportUploading = "Đang tải ảnh lên…"
    wasteReportNoteLabel = "Ghi chú"
    wasteReportNoteHint = "Mô tả tình trạng rác…"
    wasteReportUploadError = "Tải lên thất bại. Vui lòng thử lại."
    wasteReportPermissionNeeded = "Cần quyền truy cập camera"
    wasteReportGrantPermission = "Cấp quyền Camera"
    wasteReportAnonymous = "Ẩn danh"
    wasteReportWardLabel = "Tên phường / khu vực"
    wasteReportTitle = "Báo cáo rác"
    wasteReportSubtitle = "Gửi & xem báo cáo rác"
    wasteReportScanTitle = "Báo cáo rác"

    // ── Khảo sát ban đầu ───────────────────────────────────────────────────────
    preAppSurveyBadgeTitle = "Hoàn thành khảo sát hồ sơ"
    preAppSurveyBadgeDesc = "Giúp chúng tôi cá nhân hoá trải nghiệm GreenMind của bạn"
    preAppSurveyTitle = "Khảo sát hồ sơ"
    preAppSurveySubtitle = "Chỉ 13 câu hỏi nhanh về thói quen của bạn"
    preAppSurveyStep = { cur, total -> "Câu $cur / $total" }
    preAppSurveyNext = "Tiếp theo"
    preAppSurveyPrev = "Quay lại"
    preAppSurveySubmit = "Gửi"
    preAppSurveySubmitting = "Đang gửi…"
    preAppSurveyDone = "Hoàn thành! 🎉"
    preAppSurveyDoneDesc = "Hồ sơ của bạn đã được thiết lập. Chúng tôi sẽ dùng câu trả lời để cá nhân hoá gợi ý và thông tin."
    preAppSurveyStartButton = "Bắt đầu khảo sát"

    // ── General ─────────────────────────────────────────────────────────────────
    backArrow = "←"
    menuIcon = "☰"
    userFallback = "Người dùng"
    roleSwitch = "⇄"
    retry = "Thử lại"
    chevronRight = "›"
    stationaryLabel = "đứng yên"
    hour = "Giờ"
    minute = "Phút"
    updateTime = "Giờ cập nhật"
    updating = "Đang cập nhật…"
    updateAll = "↺  Cập nhật tất cả"
    noRecordsYet = "Chưa có bản ghi nào — theo dõi sẽ hiện danh sách."
    googleLogo = "G"
    searchIcon = "🔍"
    flagEmoji = "🏳"
    checkmark = "✓"

    // ── Splash ─────────────────────────────────────────────────────────────────
    splashLogo = "🌱"

    // ── Blog ───────────────────────────────────────────────────────────────────
    noContentAvailable = "Không có nội dung."

    // ── Settings ────────────────────────────────────────────────────────────────
    metricsAutoUpdate = "Cập nhật chỉ số tự động"
    autoUpdateMetrics = "Bật cập nhật chỉ số"
    autoUpdateMetricsDesc = "Tự động làm mới tất cả chỉ số mỗi ngày"
    updateTimeDesc = "Cập nhật hàng ngày sẽ chạy vào lúc này"
    dailySpending = "Chi tiêu hàng ngày"
    nightOut = "Ra ngoài buổi tối"
    spendVariability = "Mức biến động chi tiêu"
    brandNovelty = "Độ mới của thương hiệu"
    listAdherence = "Tuân thủ danh sách"
    dailyDistance = "Quãng đường hàng ngày"
    novelLocationRatio = "Tỉ lệ địa điểm mới"
    publicTransitRatio = "Tỉ lệ phương tiện công cộng"
    interval15s = "15s"
    interval30s = "30s"
    interval55s = "55s"
    interval2m = "2p"
    interval5m = "5p"
    speedWalk = "🚶"
    speedRun = "🏃"
    speedCycle = "🚴"
    greenMind = "GreenMind"
    version = "v1.0.0"
    noGpsRecords = "Chưa có bản ghi nào — theo dõi sẽ hiện danh sách."

    // ── Household Dashboard ─────────────────────────────────────────────────────
    householdDashboard = "Bảng điều khiển hộ gia đình"
    memberCount = { n -> "$n thành viên" }
    noScoreHistory = "Chưa có lịch sử điểm"
    viewWasteImpactAnalysis = "📊  Xem phân tích ảnh hưởng rác"
    noHouseholdScans = "Chưa có lượt quét hộ gia đình"
    noWasteReports = "Chưa có báo cáo rác nào"
    householdSettings = "⚙  Cài đặt hộ gia đình"
    currentScore = "Điểm hiện tại"
    deltaFromLastScan = { d -> "+$d so với lần quét trước" }
    scoreHistory = "Lịch sử điểm"
    scoreTransition = { prev, _ -> "$prev →" }
    scoreLabel = { s -> "Điểm: $s" }
    detectedItems = "Mục đã phát hiện"
    scoreBreakdown = "Chi tiết điểm"
    noDetailAvailable = "Không có chi tiết cho lần ghi này."
    scanHistoryScreen = "Lịch sử quét"
    myScanReports = "Báo cáo quét của tôi"
    myWasteReports = "Báo cáo rác của tôi"

    // ── Create Household Modal ──────────────────────────────────────────────────
    setLocation = "Đặt vị trí"
    houseNumberOptional = "Số nhà (tùy chọn)"
    houseNumberPlaceholder = "VD: 42, 12A"
    addressFromGps = "Địa chỉ từ GPS"
    coordinatesFromGps = { lat, lon -> "Tọa độ GPS: $lat, $lon" }
    locationRequired = "📍 Cần vị trí"
    locationPermissionNotGranted = "Quyền truy cập vị trí chưa được cấp. Vui lòng cho phép truy cập."
    grantLocationPermission = "Cấp quyền Vị trí"
    locationTrackingOff = "Theo dõi vị trí đang tắt. Bật để tự động phát hiện địa chỉ."
    enableLocationTracking = "Bật theo dõi vị trí"
    waitingForGps = "Đang chờ tín hiệu GPS... Vui lòng bật dịch vụ vị trí."

    // ── Household Settings ─────────────────────────────────────────────────────
    householdSettingsTitle = "Cài đặt hộ gia đình"
    addMember = "Thêm thành viên"
    userId = "ID người dùng"
    userIdPlaceholder = "Nhập ID thành viên"
    memberAddedSuccess = "Thêm thành viên thành công"
    userIdCannotBeEmpty = "ID người dùng không được để trống"
    failedToAddMember = "Thêm thành viên thất bại"
    members = "Thành viên"
    noMembersYet = "Chưa có thành viên nào"
    failedToRemoveMember = "Xóa thành viên thất bại"
    removeMember = "✕"

    // ── Catalogue ──────────────────────────────────────────────────────────────
    environmentalImpact = "Tác động môi trường"
    environmentalImpactDesc = "Hiển thị điểm CO₂, vi nhựa và mức ô nhiễm từ quét rác"
    allUsersLabel = "Tất cả người dùng"

    // ── Energy ─────────────────────────────────────────────────────────────────
    kWh = "kWh"
    vsYesterday = { d -> "▲$d so với hôm qua" }

    // ── Meal Detail ─────────────────────────────────────────────────────────────
    plant = "thực vật"
    unnamedMeal = "Bữa ăn không tên"
    thresholdLow = "< 40%"
    thresholdOk = "40–69%"
    thresholdGood = "≥ 70%"
    thresholdRange = { _, _, _ -> "< 40% / 40–69% / ≥ 70%" }
    aimForPlant = "🌿  Hãy nhắm đến ít nhất 70% thực vật trong bữa ăn để giảm dấu chân carbon."

    // ── Meal List ──────────────────────────────────────────────────────────────
    emptyMealEmoji = "??"
    scanMealEmoji = "??"

    // ── Todos ──────────────────────────────────────────────────────────────────
    addTodo = "+"
    dismissError = "✕"
    emptyTodosEmoji = "🌱"
    expand = "▸"
    aiWand = "✨"
    deleteTodo = "✕"
    addSubtask = "+"

    // ── Survey ─────────────────────────────────────────────────────────────────
    pleaseSelectAnswer = "Vui lòng chọn một câu trả lời"
    errorSubmitting = "Gửi thất bại"
    networkError = "Lỗi mạng. Vui lòng thử lại."
    surveyBy = { author -> "bởi $author" }

    // ── Waste Impact ────────────────────────────────────────────────────────────
    refresh = "↻ Làm mới"
    scans = "Lượt quét"
    itemsDetected = "Mục đã phát hiện"
    avgEcoScore = "Điểm sinh thái TB"
    greenScoreDescription = "Điểm xanh từ dữ liệu hộ gia đình"
    basedOnScans = { n -> "Dựa trên $n lượt quét có dữ liệu ảnh hưởng" }
    averagePollutionImpact = "Tác động ô nhiễm trung bình"
    topPollutantsAvg = "Chất ô nhiễm hàng đầu (TB)"
    pollutantBreakdownAvg = "Chi tiết chất ô nhiễm (TB)"
    scanDetail = "Chi tiết lượt quét"
    pollutionImpact = "Tác động ô nhiễm"
    objects = "Vật thể"
    ecoScoreLabel = { s -> "Điểm sinh thái" }
    air = "Không khí"
    water = "Nước"
    soil = "Đất"
    excellentMinimalImpact = "Xuất sắc — tác động tối thiểu"
    goodLowImpact = "Tốt — tác động thấp"
    fairModerateImpact = "Khá — tác động trung bình"
    poorHighImpact = "Kém — tác động môi trường cao"
    scanHistoryRow = { n, author -> "$n mục · bởi $author" }
    noEcoScore = "—"
    pollutantCO2 = "CO₂"
    pollutantDioxin = "Dioxin"
    pollutantMicroplastic = "Vi nhựa"
    pollutantToxic = "Hóa chất độc"
    pollutantNonBiodegradable = "Không phân hủy sinh học"
    pollutantNOx = "NOₓ"
    pollutantSO2 = "SO₂"
    pollutantCH4 = "CH₄"
    pollutantPM25 = "PM2.5"
    pollutantLead = "Chì (Pb)"
    pollutantMercury = "Thủy ngân (Hg)"
    pollutantCadmium = "Cadmi (Cd)"
    pollutantNitrate = "Nitrat"
    pollutantChemicalResidue = "Dư lượng hóa chất"
    pollutantStyrene = "Styrene"
    airIcon = "💨"
    waterPollutionLabel = "Ô nhiễm nước"
    soilPollutionLabel = "Ô nhiễm đất"
    greenScoreHistory = "Lịch sử Điểm Xanh"
    scanHistoryTitle = "Lịch sử lượt quét"

    // ── Waste Analytics ────────────────────────────────────────────────────────
    wasteAnalyticsTitle = "📈 Phân tích rác thải"
    householdTrends = { p -> "Xu hướng hộ gia đình · xem theo $p" }
    totalWaste = "Tổng rác thải"
    peak = { label -> "Đỉnh ($label)" }
    avgAir = "KK trung bình"
    pollutionImpactTrends = "Xu hướng tác động ô nhiễm"
    airWaterSoil = "KK · Nước · Đất"
    keyPollutants = "Chất ô nhiễm chính"
    co2DioxinMicroplastic = "CO₂ · Dioxin · Vi nhựa"
    insight = "📊 Nhận định"
    wasteVolume = "Khối lượng rác"
    kilograms = "Kilôgam"
    valueLabelKg = { v -> "${v.fmt(1)}kg" }
    seriesHidden = "Đã ẩn chuỗi"
    noSeriesSelected = "Chưa chọn chuỗi nào"
    seriesAir = "Không khí"
    seriesWater = "Nước"
    seriesSoil = "Đất"
    seriesCO2 = "CO₂"
    seriesDioxin = "Dioxin"
    seriesMicroplastic = "Vi nhựa"
    periodHour = "giờ"
    periodDay = "ngày"
    periodWeek = "tuần"
    periodMonth = "tháng"
    periodLabel = { label -> label }

    // ── Home components ─────────────────────────────────────────────────────────
    surveyBadgeIcon = "📋"
    errorDisplay = { msg -> "⚠️  $msg" }
    unknownLocation = "Vị trí không xác định"
    campaignActive = "🟢"
    campaignInactive = "📅"
    campaignDescription = { desc -> "📝 $desc" }
    dateRange = { start, end -> "🕐 $start → $end" }
    locationRadius = { lat, lng, r -> "📍 $lat, $lng  ·  bán kính ${r}m" }
    registeredStatus = { s -> "✓ $s" }
    checkedInStatus = { s -> "📍 $s" }
    completedStatus = { s -> "✅ $s" }
    pollutionTrendTitle = "Xu hướng ô nhiễm"
    pollutionTrendSubtitle = "1 tháng · khoảng 7 ngày"
    co2Legend = "CO₂"
    ch4Legend = "CH₄"
    noxLegend = "NOₓ"
    percentYAxis = "%"

    // ── Waste Sort ──────────────────────────────────────────────────────────────
    wasteScanned = "Đã quét"
    wasteSorted = "Đã phân loại"
    wasteBroughtOut = "Đã đưa ra"
    wasteCollected = "Đã thu gom"

    // ── Waste Sort List ─────────────────────────────────────────────────────────
    localScans = "Quét cục bộ"
    scanCount = { n -> "$n lượt quét" }
    uploadImage = "Tải ảnh lên"
    takePhoto = "Chụp ảnh"
    myScanReportsTitle = "Báo cáo quét của tôi"
    reportCount = { n -> "$n báo cáo" }
    noScanReports = "Không tìm thấy báo cáo quét"
    fabCollapse = "✕"
    fabExpand = "+"

    // ── Scan Detail ─────────────────────────────────────────────────────────────
    ecoScore = "Điểm sinh thái"
    noScoreYet = "Chưa có điểm"
    deltaPrefix = { d -> "+$d so với lần quét trước" }
    markAsSorted = "Đánh dấu đã phân loại"
    markAsBroughtOut = "Đánh dấu đã đưa ra"
    waitingForCollector = "⏳  Đang chờ thu gom"
    collectedComplete = "✅  Đã thu gom — hoàn thành!"
    byCategory = "Theo loại"
    categoryTab = { label, count -> "$label ($count)" }

    // ── Error Log ───────────────────────────────────────────────────────────────
    errorLog = { n -> "🔴  Nhật ký lỗi ($n)" }
    clear = "Xóa"
    noErrors = "Không có lỗi nào."
    expandIndicator = "›"
    message = "THÔNG ĐIỆP"
    stackTrace = "DẤU VẾT NGĂN XẾP"
    truncated = "(cắt ngắn)"
    errorLevel = "L"
    warningLevel = "C"

    // ── Profile ──────────────────────────────────────────────────────────────────
    profileUserIcon = "👤"
    profileRoleHousehold = "🏠"
    profileRoleCollector = "🚛"
    profileRoleVolunteer = "🤝"
    profileLocation = { loc -> "📍  $loc" }
    plantRatio = "tỉ lệ thực vật"
    greenSpend = "chi tiêu xanh"
    habitProfile = "Hồ sơ thói quen"
    profileMealsIcon = "🍽️"
    profileBillsIcon = "🧾"
}