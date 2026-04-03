package com.vodang.greenmind.store

import com.vodang.greenmind.api.wastedetect.WasteDetectImpact
import com.vodang.greenmind.api.wastedetect.WasteDetectItem
import com.vodang.greenmind.api.wastedetect.WasteDetectResponse
import com.vodang.greenmind.wastesort.WasteSortEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.MainScope

private val mockEntry = WasteSortEntry(
    id           = "0874845e109d4671b240de931ad3d05f",
    imageUrl     = "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775233023/yolo_detect/detect/0874845e109d4671b240de931ad3d05f.jpg",
    totalObjects = 20,
    grouped      = mapOf(
        "recyclable" to listOf(
            "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229793/yolo_segments/segments/5a773619de2f456d8f1125414e674932.png",
            "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229794/yolo_segments/segments/1c9467c3460a4c8384e62e1a2fc89c44.png",
            "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229794/yolo_segments/segments/784f03c4f3ea4ef398759eb6f7c74557.png",
            "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229795/yolo_segments/segments/46fc23d93e0845e4a39711312ffbd382.png",
            "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229796/yolo_segments/segments/6cfd851246704eee9d2879692e5831ce.png",
            "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229797/yolo_segments/segments/40dc1771ad3148c8a546573d8809b48b.png",
            "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229798/yolo_segments/segments/fda0c521ac864860a57ffe6280aeef67.png",
            "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229799/yolo_segments/segments/f1c3bdbeebc24381b741b73815386d66.png",
            "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229799/yolo_segments/segments/be5bcce1b09343aea2a26c74d7e65ee5.png",
            "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229800/yolo_segments/segments/f18643d656564409a03c2f959fcb1901.png",
            "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229801/yolo_segments/segments/cbce16b89a8042d4b1824af23b240f84.png",
            "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229802/yolo_segments/segments/899fdbd04b9a4292b07e313759552488.png",
            "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229803/yolo_segments/segments/f3fe12a5832b4489bc7d2c8c33f2cd75.png",
            "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229804/yolo_segments/segments/f97951adf882430c830e10a8fd976da3.png",
            "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229805/yolo_segments/segments/53376faf83244bb0a40823bdc7dd5fb1.png",
            "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229806/yolo_segments/segments/98f0a5c943774825aed021a9ff197e78.png",
            "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229807/yolo_segments/segments/ea2b92dce5a644f1976786d50d2b34d1.png",
            "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229808/yolo_segments/segments/326544ca391f4cbcbc531a6aee7ea8e3.png",
            "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229809/yolo_segments/segments/4a953c3efe0c4f4ca80d801b17c1f9b5.png"
        ),
        "residual" to listOf(
            "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229805/yolo_segments/segments/2dc9dffad09c4d149b60bd449dce3498.png"
        )
    ),
    createdAt    = "2026-04-03",
    scannedBy    = "Demo",
    pollutantResult = WasteDetectResponse(
        items = listOf(
            WasteDetectItem(name = "Glass bottle",          quantity = 12, area = 15351),
            WasteDetectItem(name = "Clear plastic bottle",  quantity = 1,  area = 1798),
            WasteDetectItem(name = "Drink can",             quantity = 6,  area = 6170),
            WasteDetectItem(name = "Styrofoam piece",       quantity = 1,  area = 3778),
        ),
        totalObjects = 20,
        imageUrl     = "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775233023/yolo_detect/detect/0874845e109d4671b240de931ad3d05f.jpg",
        pollution    = mapOf(
            "non_biodegradable" to 0.6783826032636188,
            "PM2.5"             to 0.28007494481959816,
            "CO2"               to 0.4601208758323635,
            "dioxin"            to 0.2875486336519803,
            "microplastic"      to 0.35368149204467525,
            "toxic_chemicals"   to 0.3001061587841517,
            "NOx"               to 0.25334234208300827,
            "SO2"               to 0.25334234208300827,
            "styrene"           to 0.27811839930088283,
            "CH4"               to 0.0,
            "Pb"                to 0.0,
            "Hg"                to 0.0,
            "Cd"                to 0.0,
            "nitrate"           to 0.0,
            "chemical_residue"  to 0.0,
        ),
        impact = WasteDetectImpact(
            airPollution   = 0.25573818974499307,
            waterPollution = 0.058946915340779206,
            soilPollution  = 0.2683814422322214,
        ),
    ),
)

object WasteSortStore {
    val storeScope = MainScope()
    private val _entries = MutableStateFlow<List<WasteSortEntry>>(listOf(mockEntry))
    val entries: StateFlow<List<WasteSortEntry>> = _entries.asStateFlow()

    fun add(entry: WasteSortEntry) {
        _entries.value = listOf(entry) + _entries.value
    }

    fun updatePollutant(id: String, pollutant: WasteDetectResponse) {
        _entries.value = _entries.value.map { entry ->
            if (entry.id == id) entry.copy(pollutantResult = pollutant) else entry
        }
    }
}
