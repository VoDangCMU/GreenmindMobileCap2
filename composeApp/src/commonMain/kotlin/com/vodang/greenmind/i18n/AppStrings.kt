package com.vodang.greenmind.i18n

import androidx.compose.runtime.compositionLocalOf

data class AppStrings(
    val langCode: String,

    // ── App ──────────────────────────────────────────────────────────────
    val appName: String,
    val appSubtitle: String,

    // ── Common ───────────────────────────────────────────────────────────
    val back: String,
    val cancel: String,
    val save: String,
    val close: String,
    val select: String,
    val delete: String,
    val today: String,
    val or: String,
    val show: String,
    val hide: String,

    // ── Login ────────────────────────────────────────────────────────────
    val signIn: String,
    val emailAddress: String,
    val emailPlaceholder: String,
    val password: String,
    val rememberMe: String,
    val forgotPassword: String,
    val loginError: String,
    val loginButton: String,
    val loggingIn: String,
    val continueWithGoogle: String,
    val noAccount: String,
    val signUp: String,
    val chooseAccount: String,
    val noSavedAccounts: String,
    val addAccount: String,

    // ── Register ─────────────────────────────────────────────────────────
    val createAccount: String,
    val stepInfo: String,
    val stepPassword: String,
    val stepAddress: String,
    val yourInformation: String,
    val setPassword: String,
    val addressAndTerms: String,
    val fullName: String,
    val gender: String,
    val male: String,
    val female: String,
    val other: String,
    val next: String,
    val confirmPassword: String,
    val passwordMismatch: String,
    val address: String,
    val road: String,
    val city: String,
    val acceptTerms: String,
    val register: String,
    val alreadyHaveAccount: String,

    // ── Drawer ───────────────────────────────────────────────────────────
    val home: String,
    val todos: String,
    val surveys: String,
    val settings: String,
    val editProfile: String,

    // ── Top bar ──────────────────────────────────────────────────────────
    val greeting: (String) -> String,
    val welcomeBack: String,

    // ── User type ────────────────────────────────────────────────────────
    val chooseRole: String,
    val householdRole: String,
    val collectorRole: String,
    val volunteerRole: String,

    // ── Language ─────────────────────────────────────────────────────────
    val language: String,
    val langVietnamese: String,
    val langEnglish: String,

    // ── Household dashboard ──────────────────────────────────────────────
    val todayOverview: String,
    val wasteSort: String,
    val wasteSortValue: String,
    val garbageDrop: String,
    val garbageDropStatus: String,
    val electricityUsage: String,
    val electricityValue: String,
    val greenSpending: String,
    val greenSpendingValue: String,
    val greenSpendingMonth: String,
    val greenMealMetric: String,
    val greenMealPercent: String,
    val greenMealSubtitle: String,
    val todosMetric: String,
    val todosValue: String,
    val walkDistance: String,
    val walkValue: String,
    val features: String,
    val wasteSortDesc: String,
    val garbageDropDesc: String,
    val todosDesc: String,
    val electricityChartDesc: String,
    val scanMeal: String,
    val scanMealDesc: String,

    // ── Electricity card ─────────────────────────────────────────────────
    val electricityWeekTitle: String,
    val electricityWeekSummary: String,
    val weekDays: List<String>,

    // ── Green meal card ──────────────────────────────────────────────────
    val greenMealCardTitle: String,
    val greenMealCardDesc: String,

    // ── Todos screen ─────────────────────────────────────────────────────
    val todosScreenTitle: String,
    val todosAddPlaceholder: String,
    val todosAddSubPlaceholder: String,
    val todosEmpty: String,

    // ── Todos card ───────────────────────────────────────────────────────
    val todosCardTitle: String,
    val todosCardProgress: (Int, Int) -> String,

    // ── OCEAN card ───────────────────────────────────────────────────────
    val oceanTitle: String,
    val oceanSubtitle: String,

    // ── Collector dashboard ──────────────────────────────────────────────
    val collectorTitle: String,
    val collectorShift: String,
    val collectorPointsUnit: String,
    val progressToday: String,
    val pointsCollected: (Int) -> String,
    val pointsRemaining: (Int) -> String,
    val zoneLabel: String,
    val zoneValue: String,
    val bagsLabel: String,
    val bagsEstimated: String,
    val routeLabel: String,
    val heatmapFeatureLabel: String,
    val heatmapFeatureDesc: String,
    val scheduleLabel: String,
    val scheduleDesc: String,

    // ── Heatmap card ─────────────────────────────────────────────────────
    val heatmapCardTitle: String,
    val heatLow: String,
    val heatHigh: String,

    // ── Route card ───────────────────────────────────────────────────────
    val routeCardTitle: String,
    val bagsUnit: String,

    // ── CheckIn card ─────────────────────────────────────────────────────
    val checkInCardTitle: String,
    val checkInButton: String,
    val checkInDone: String,
    val checkInDoneDesc: String,
    val nextPointLabel: String,
    val bagsEstimatedFmt: (Int) -> String,

    // ── CollectionPointRow ───────────────────────────────────────────────
    val collectedLabel: String,
    val notCollectedLabel: String,

    // ── Volunteer dashboard ──────────────────────────────────────────────
    val volunteerTitle: String,
    val volunteerWip: String,
    val volunteerSubtitle: String,
    val volunteerHoursLabel: String,
    val volunteerHoursValue: String,
    val volunteerEventsLabel: String,
    val volunteerEventsValue: String,
    val volunteerPointsLabel: String,
    val volunteerPointsValue: String,
    val volunteerEventsCardTitle: String,
    val volunteerUpcomingTitle: String,
    val volunteerJoinButton: String,
    val volunteerRegistered: String,

    // ── Profile ──────────────────────────────────────────────────────────
    val profileName: String,
    val profileEmail: String,

    // ── Survey ───────────────────────────────────────────────────────────
    val surveysDesc: String,
    val surveyMinutes: (Int) -> String,
    val surveyQuestions: (Int) -> String,
    val surveyCompleted: String,
    val surveyStart: String,
    val surveyRetake: String,
    val surveyQuestion: (Int, Int) -> String,
    val surveyNext: String,
    val surveyPrev: String,
    val surveySubmit: String,
    val surveySubmitted: String,
    val surveySubmittedDesc: String,
    val surveySelectMultiple: String,
    val surveyTypeAnswer: String,
    val surveyBackToList: String,
)

val LocalAppStrings = compositionLocalOf<AppStrings> { EnStrings }
