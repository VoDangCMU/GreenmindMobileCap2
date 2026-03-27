package com.vodang.greenmind.api.bill

import kotlinx.coroutines.delay

data class BillItem(val name: String, val amount: Double, val isGreen: Boolean)

data class BillAnalysisResult(
    val storeName: String,
    val totalAmount: Double,
    val greenAmount: Double,
    val greenRatio: Int,
    val items: List<BillItem>,
)

private val mockBills = listOf(
    BillAnalysisResult(
        storeName = "Green Garden Market",
        totalAmount = 42.50,
        greenAmount = 38.20,
        greenRatio = 90,
        items = listOf(
            BillItem("Organic spinach", 3.50, true),
            BillItem("Tofu block", 2.80, true),
            BillItem("Brown rice 1kg", 4.20, true),
            BillItem("Avocado x2", 5.00, true),
            BillItem("Almond milk", 4.70, true),
            BillItem("Reusable bag", 1.50, true),
            BillItem("Sparkling water", 2.50, false),
            BillItem("Cheese", 4.30, false),
        )
    ),
    BillAnalysisResult(
        storeName = "FreshMart Supermarket",
        totalAmount = 67.80,
        greenAmount = 40.70,
        greenRatio = 60,
        items = listOf(
            BillItem("Broccoli", 2.20, true),
            BillItem("Carrots 500g", 1.80, true),
            BillItem("Lentils 1kg", 3.50, true),
            BillItem("Oat milk", 4.20, true),
            BillItem("Chicken breast", 9.90, false),
            BillItem("Butter", 4.50, false),
            BillItem("White bread", 2.80, false),
            BillItem("Soft drink 6pk", 8.90, false),
        )
    ),
    BillAnalysisResult(
        storeName = "City Convenience",
        totalAmount = 28.60,
        greenAmount = 8.60,
        greenRatio = 30,
        items = listOf(
            BillItem("Banana", 1.50, true),
            BillItem("Apple juice", 3.20, true),
            BillItem("Beef burger patty", 7.80, false),
            BillItem("Instant noodles x3", 4.50, false),
            BillItem("Chips", 3.20, false),
            BillItem("Energy drink", 3.90, false),
            BillItem("Chocolate bar", 2.50, false),
            BillItem("Plastic bottle water", 2.00, false),
        )
    ),
    BillAnalysisResult(
        storeName = "Organic Basket",
        totalAmount = 55.30,
        greenAmount = 49.80,
        greenRatio = 90,
        items = listOf(
            BillItem("Kale bunch", 2.90, true),
            BillItem("Quinoa 500g", 6.50, true),
            BillItem("Hemp seeds", 8.20, true),
            BillItem("Tempeh", 3.80, true),
            BillItem("Coconut yogurt", 5.40, true),
            BillItem("Mixed nuts 250g", 9.80, true),
            BillItem("Kombucha", 4.20, true),
            BillItem("Free-range eggs", 5.50, false),
        )
    ),
    BillAnalysisResult(
        storeName = "QuickStop Deli",
        totalAmount = 19.40,
        greenAmount = 7.80,
        greenRatio = 40,
        items = listOf(
            BillItem("Salad wrap", 5.80, true),
            BillItem("Orange juice", 2.00, true),
            BillItem("BLT sandwich", 6.50, false),
            BillItem("Cookie", 1.80, false),
            BillItem("Soda can", 1.50, false),
            BillItem("Chewing gum", 1.80, false),
        )
    ),
)

suspend fun analyzeBill(imageBytes: ByteArray): BillAnalysisResult {
    delay(1_500) // simulate network latency
    return mockBills.random()
}
