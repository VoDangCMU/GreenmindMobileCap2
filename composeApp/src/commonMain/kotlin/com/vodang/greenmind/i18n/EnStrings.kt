package com.vodang.greenmind.i18n

import com.vodang.greenmind.fmt

val EnStrings = AppStrings("en").apply {


    appName = "GreenMind"
    appSubtitle = "Smart Environmental System"

    back = "BACK"
    cancel = "Cancel"
    save = "Save"
    close = "Close"
    select = "Select"
    delete = "Delete"
    today = "Today"
    or = "  OR  "
    show = "Show"
    hide = "Hide"

    signIn = "Sign In"
    emailAddress = "Email Address"
    emailPlaceholder = "you@example.com"
    password = "Password"
    rememberMe = "Remember me"
    forgotPassword = "Forgot password?"
    loginError = "Please enter email and password"
    loginButton = "LOGIN"
    loggingIn = "LOGGING IN..."
    continueWithGoogle = "Continue with Google"
    noAccount = "Don't have an account? "
    signUp = "Sign up"
    chooseAccount = "Choose account"
    noSavedAccounts = "No saved accounts"
    addAccount = "Add account"

    createAccount = "Create your account"
    stepInfo = "Info"
    stepPassword = "Password"
    stepAddress = "Address"
    yourInformation = "Your Information"
    setPassword = "Set Password"
    addressAndTerms = "Address & Terms"
    fullName = "Full name"
    gender = "Gender"
    male = "Male"
    female = "Female"
    other = "Other"
    next = "NEXT"
    confirmPassword = "Confirm password"
    passwordMismatch = "Passwords do not match"
    address = "Address"
    road = "Road"
    country = "Country"
    selectCountry = "Select a country"
    searchCountry = "Search countries..."
    city = "City"
    selectCity = "Select a city"
    searchCity = "Search cities..."
    acceptTerms = "I accept the terms and conditions"
    register = "REGISTER"
    alreadyHaveAccount = "Already have an account? "

    home = "Home"
    todos = "Todos"
    surveys = "Surveys"
    blog = "Blog"
    settings = "Settings"
    editProfile = "Edit Profile"
    catalogue = "Feature Catalogue"
    catalogueSubtitle = "All available app features"
    logout = "Log out"

    greeting = { name -> "Hello, $name" }
    welcomeBack = "Welcome back"

    chooseRole = "Choose role"
    householdRole = "Household"
    collectorRole = "Collector"
    volunteerRole = "Volunteer"

    language = "Language"
    langVietnamese = "Tiếng Việt"
    langEnglish = "English"

    todayOverview = "Today's overview"
    wasteSort = "Waste sorting"
    wasteSortValue = "3 types"
    garbageDrop = "Waste report"
    garbageDropStatus = "Not yet"
    electricityUsage = "Electricity"
    electricityValue = "4.2 kWh"
    greenSpending = "Green spending"
    greenSpendingValue = "320k ₫"
    greenSpendingMonth = "March"
    greenMealMetric = "Green meal"
    greenMealPercent = "68%"
    greenMealSubtitle = "Veggies today"
    todosMetric = "Todos"
    todosValue = { done, total -> "$done/$total done" }
    walkDistance = "Distance"
    walkValue = "4.7 km"
    features = "Features"
    wasteSortDesc = "Take photo to classify"
    garbageDropDesc = "Check-in waste report"
    wasteReport = "Waste report"
    wasteReportDesc = "Submit & view waste reports"
    todosDesc = "To-do list"
    electricityChartDesc = "View electricity chart"
    scanMeal = "Scan meal"
    scanMealDesc = "Take photo of meal"
    scanBill = "Scan bill"
    scanBillDesc = "Scan your receipt"
    wasteToday = "Waste today"
    wasteBagsCount = { n -> "$n bags" }
    greenerLabel = "Greener score"
    greenerUp = "↑ Getting greener"
    greenerDown = "↓ Needs improvement"
    noActivityToday = "No activity yet"

    electricityWeekTitle = "Electricity this week"
    electricityWeekSummary = "Week total: 31.7 kWh  |  vs last week: ▲5%"
    weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    energyTodayLabel = "Today's usage"
    energyMonthTotal = "Month total"
    energyAvgDaily = "Daily avg"
    energyTips = "Energy saving tips"
    energyTipsList = listOf(
        "💡 Switch to LED bulbs — they use 75% less energy",
        "🌡️ Set your AC to 26°C to cut cooling costs",
        "🔌 Unplug devices on standby — they still draw power",
        "🪟 Use natural light during the day when possible",
        "🧺 Wash clothes in cold water to save up to 90% of heating energy",
    )

    greenMealCardTitle = "Green meal today"
    greenMealCardDesc = "Vegetables make up 68% of the meal — Great! 🌟"

    todosScreenTitle = "Todos"
    todosAddPlaceholder = "New todo..."
    todosAddSubPlaceholder = "New subtask..."
    todosEmpty = "No todos yet"

    todosCardTitle = "Today's tasks"
    todosCardProgress = { done, total -> "$done/$total completed" }

    oceanTitle = "OCEAN Score"
    oceanSubtitle = "Environmental personality"

    collectorTitle = "Garbage Collector"
    collectorShift = "Monday, 17/03/2026 · Morning shift · Đà Nẵng"
    collectorPointsUnit = "points"
    progressToday = "Today's progress"
    pointsCollected = { n -> "$n points collected" }
    pointsRemaining = { n -> "$n remaining" }
    zoneLabel = "Zones"
    zoneValue = "3 zones"
    bagsLabel = "Bags"
    bagsEstimated = "Estimated"
    routeLabel = "Route"
    heatmapFeatureLabel = "Waste heatmap"
    heatmapFeatureDesc = "View collection point map"
    scheduleLabel = "Schedule"
    scheduleDesc = "Garbage collection route"

    heatmapCardTitle = "🗺️ Waste heatmap"
    heatLow = "Low"
    heatHigh = "High"

    routeCardTitle = "📍 Collection route"
    bagsUnit = "bags"

    checkInCardTitle = "✅ Check-in collection"
    checkInButton = "Check-in at this point"
    checkInDone = "All points done!"
    checkInDoneDesc = "You've collected garbage at all points today."
    nextPointLabel = "Next point"
    bagsEstimatedFmt = { n -> "$n bags estimated" }

    collectedLabel = "Collected"
    notCollectedLabel = "Pending"

    volunteerTitle = "Volunteer"
    volunteerWip = "Features are being developed.\nComing soon!"
    volunteerSubtitle = "Environmental protection · Da Nang"
    volunteerHoursLabel = "Hours volunteered"
    volunteerHoursValue = "24h"
    volunteerEventsLabel = "Events joined"
    volunteerEventsValue = "8"
    volunteerPointsLabel = "Points earned"
    volunteerPointsValue = "1,240"
    volunteerEventsCardTitle = "🗓️ Active events"
    volunteerUpcomingTitle = "📅 Upcoming events"
    volunteerJoinButton = "Register"
    volunteerRegistered = "Registered"

    profileName = "User Name"
    profileEmail = "user@example.com"
    profileTitle = "My Profile"
    todayJourney = "Today's Journey"
    walkTimeLabel = "Walk time"
    walkTimeMin = { n -> if (n < 60) "~${n}m" else "~${n/60}h ${n%60}m" }
    caloriesLabel = "Calories"
    caloriesKcal = { n -> "~${n} kcal" }
    personalInfo = "Personal Info"
    genderLabel = "Gender"
    ageLabel = "Age"
    locationLabel = "Location"
    roleLabel = "Role"
    notSet = "Not set"
    ecoToday = "Eco Activity Today"
    mealsToday = { n -> "$n meals" }
    billsToday = { n -> "$n bills" }

    settingsLocation            = "Location Tracking"
    settingsTrackingEnabled     = "Tracking active"
    settingsTrackingEnabledDesc = "Record GPS positions in the background"
    settingsInterval            = "Update interval"
    settingsIntervalDesc        = "How often your position is saved"
    settingsMinMove             = "Stationary threshold"
    settingsMinMoveDesc         = { m -> "Below ${m}m = standing still" }
    settingsSpeedFilter         = "Speed filter"
    settingsSpeedFilterDesc     = { v -> "Movements faster than ${v.fmt(1)} m/s are treated as vehicle travel" }
    settingsSpeedWalk           = "Walk"
    settingsSpeedRun            = "Run"
    settingsSpeedCycle          = "Cycle"
    settingsGeneral             = "General"
    settingsAbout               = "About"
    settingsHowWorks            = "How tracking works"
    settingsHowWorksBody        = { secs, meters, speed ->
        "GPS is sampled every ${secs}s. Points within ${meters}m are treated as stationary. " +
        "Movements faster than ${speed.fmt(1)} m/s are excluded as vehicle travel."
}
    settingsVersion             = "Version"

    surveysDesc = "Answer surveys to help improve your community"
    blogDesc = "Read articles and tips about green living"
    blogSubtitle = "Green living articles & tips"
    blogEmpty = "No posts yet. Check back soon!"
    surveyMinutes = { n -> "~$n min" }
    surveyQuestions = { n -> "$n questions" }
    surveyCompleted = "✓ Done"
    surveyStart = "Start →"
    surveyRetake = "Retake →"
    surveyQuestion = { cur, total -> "Question $cur of $total" }
    surveyNext = "Next"
    surveyPrev = "Previous"
    surveySubmit = "Submit"
    surveySubmitted = "Thank you!"
    surveySubmittedDesc = "Your responses have been recorded. You're helping make your community greener."
    surveySelectMultiple = "Select all that apply"
    surveyTypeAnswer = "Type your answer here..."
    surveyBackToList = "Back to surveys"

    mealScreenTitle = "My Meals"
    mealListEmpty = "No meals recorded yet"
    mealScanTitle = "Scan Meal"
    mealCapture = "Capture"
    mealUpload = "Upload from Gallery"
    mealRetake = "Retake"
    mealAnalyze = "Analyze"
    mealSave = "Save meal"
    mealNameLabel = "Meal name"
    mealAnalyzing = "Analyzing plant ratio..."
    mealScanAgain = "Scan again"
    mealPlantRatio = { pct -> "Plant ratio: $pct%" }
    mealRatioGood = "Great plant ratio! 🌟"
    mealRatioOk = "Good effort 🌿"
    mealRatioLow = "Try adding more vegetables 🥦"
    mealPermissionNeeded = "Camera permission is required"
    mealGrantPermission = "Grant Camera Permission"
    mealError = "Analysis failed. Try again."

    // ── Bill scan ─────────────────────────────────────────────────────────────────
    billScreenTitle = "Bill History"
    billListEmpty = "No bills scanned yet"
    billScanTitle = "Scan Bill"
    billScanHint = "Point camera at your receipt"
    billCapture = "Capture"
    billUpload = "Upload from Gallery"
    billRetake = "Retake"
    billAnalyze = "Analyze"
    billSave = "Save bill"
    billStoreNameLabel = "Store name"
    billAnalyzing = "Analyzing bill..."
    billScanAgain = "Scan again"
    billGreenRatio = { pct -> "Green spending: $pct%" }
    billGreenAmount = { amt -> "Green: $${amt.fmt(2)}" }
    billRatioGood = "Excellent eco shopping! 🌟"
    billRatioOk = "Good green choices 🌿"
    billRatioLow = "Try buying more eco-friendly products 🌱"
    billTotal = "Total"
    billGreenSpend = "Green spend"
    billItems = "Items"
    billPermissionNeeded = "Camera permission is required"
    billGrantPermission = "Grant Camera Permission"
    billError = "Analysis failed. Try again."

    // ── Waste report scan ──────────────────────────────────────────────────────
    wasteReportLabel = "Waste type"
    wasteReportWeightLabel = "Estimated weight (kg)"
    wasteReportContinue = "Continue"
    wasteReportMyTab = "My Reports"
    wasteReportAllTab = "All Reports"
    wasteReportEmpty = "No reports yet.\nTap + to submit your first waste report."
    wasteReportCapture = "Tap to take a photo of the waste"
    wasteReportRetake = "Retake"
    wasteReportSubmit = "Submit Report"
    wasteReportUploading = "Uploading photo…"
    wasteReportNoteLabel = "Note"
    wasteReportNoteHint = "Describe the waste situation…"
    wasteReportUploadError = "Upload failed. Please try again."
    wasteReportPermissionNeeded = "Camera permission is required"
    wasteReportGrantPermission = "Grant Camera Permission"
    wasteReportAnonymous = "Anonymous"
    wasteReportWardLabel = "Ward / Area name"
    wasteReportTitle = "Waste Report"
    wasteReportSubtitle = "Submit & view waste reports"
    wasteReportScanTitle = "Report Waste"
}