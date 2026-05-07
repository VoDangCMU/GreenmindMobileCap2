package com.vodang.greenmind.scandetail

import com.vodang.greenmind.api.households.DetectItemDto
import com.vodang.greenmind.api.households.DetectItemMassDto
import com.vodang.greenmind.api.households.DetectTrashHistoryDto
import com.vodang.greenmind.api.households.DetectPollutionDto
import com.vodang.greenmind.api.households.DetectImpactDto
import com.vodang.greenmind.api.households.GreenScoreEntryDto
import com.vodang.greenmind.api.wastedetect.WasteDetectItem
import com.vodang.greenmind.api.wastedetect.WasteDetectImpact
import com.vodang.greenmind.api.wastedetect.WasteDetectResponse
import com.vodang.greenmind.householdwaste.groupStatus
import com.vodang.greenmind.householdwaste.parseWasteSortStatus
import com.vodang.greenmind.wastesort.WasteSortEntry
import com.vodang.greenmind.wastesort.WasteSortStatus
import com.vodang.greenmind.fmt

// ── Unified data model ─────────────────────────────────────────────────────────

data class ScanDetailData(
    val id: String,
    val imageUrl: String,
    val createdAt: String,
    val scannedBy: String,
    val status: WasteSortStatus,
    // detect_trash
    val totalObjects: Int? = null,
    val items: List<ScanItem>? = null,
    val annotatedImageUrl: String? = null,
    val aiAnalysisUrl: String? = null,
    // predict_pollutant_impact
    val pollution: Map<String, Double>? = null,
    val impact: ScanImpact? = null,
    // total_mass
    val totalMassKg: Double? = null,
    val itemsMass: List<ScanItemMass>? = null,
    val depthMapUrl: String? = null,
    // green score
    val greenScore: GreenScoreEntryDto? = null,
    val isGreenScoreLoading: Boolean = false,
    // local store only (category grouping)
    val grouped: Map<String, List<String>> = emptyMap(),
    // backend id for API calls
    val backendId: String? = null,
)

data class ScanImpact(
    val air: Double,
    val water: Double,
    val soil: Double,
)

data class ScanItem(
    val name: String,
    val quantity: Int,
    val massKg: Double? = null,
)

data class ScanItemMass(
    val name: String,
    val massKg: Double,
)

enum class DisplayMode { FULL_SCREEN, BOTTOM_SHEET }

// ── Adapters ──────────────────────────────────────────────────────────────────

/** Adapt WasteSortEntry (local store entry) to ScanDetailData */
fun WasteSortEntry.toScanDetailData(): ScanDetailData {
    val impactData = pollutantResult?.impact
    return ScanDetailData(
        id = id,
        imageUrl = imageUrl,
        createdAt = createdAt,
        scannedBy = scannedBy,
        status = status,
        totalObjects = totalObjects,
        items = pollutantResult?.items?.map { it.toScanItem() },
        pollution = pollutantResult?.pollution,
        impact = impactData?.let { ScanImpact(it.airPollution, it.waterPollution, it.soilPollution) },
        totalMassKg = totalMassKg,
        greenScore = greenScoreResult,
        grouped = grouped,
        backendId = backendId,
    )
}

/** Adapt WasteDetectItem to ScanItem */
fun WasteDetectItem.toScanItem() = ScanItem(name = name, quantity = quantity, massKg = null)

/** Adapt DetectItemDto to ScanItem */
fun DetectItemDto.toScanItem() = ScanItem(name = name, quantity = quantity, massKg = massKg)

/** Adapt DetectItemMassDto to ScanItemMass */
fun DetectItemMassDto.toScanItemMass() = ScanItemMass(name = name, massKg = massKg)

/** Adapt DetectPollutionDto to flat map */
fun DetectPollutionDto.toFlatMap(): Map<String, Double> = buildMap {
    cd?.let               { put("Cd", it) }
    hg?.let               { put("Hg", it) }
    pb?.let               { put("Pb", it) }
    ch4?.let              { put("CH4", it) }
    co2?.let              { put("CO2", it) }
    nox?.let              { put("NOx", it) }
    so2?.let              { put("SO2", it) }
    pm25?.let             { put("PM2.5", it) }
    dioxin?.let           { put("dioxin", it) }
    nitrate?.let          { put("nitrate", it) }
    styrene?.let          { put("styrene", it) }
    microplastic?.let     { put("microplastic", it) }
    toxicChemicals?.let   { put("toxic_chemicals", it) }
    chemicalResidue?.let  { put("chemical_residue", it) }
    nonBiodegradable?.let { put("non_biodegradable", it) }
}.filterValues { it > 0.0 }

/** Adapt DetectImpactDto to ScanImpact */
fun DetectImpactDto.toScanImpact() = ScanImpact(
    air = airPollution ?: 0.0,
    water = waterPollution ?: 0.0,
    soil = soilPollution ?: 0.0,
)

/** Adapt DetectTrashHistoryDto (single record) to ScanDetailData */
fun DetectTrashHistoryDto.toScanDetailData(): ScanDetailData {
    val impactData = impact?.toScanImpact()
    val pollutionData = pollution?.toFlatMap()
    return ScanDetailData(
        id = id,
        imageUrl = imageUrl,
        createdAt = createdAt?.take(10) ?: "",
        scannedBy = detectedBy?.fullName ?: detectedBy?.username ?: "",
        status = parseWasteSortStatus(status),
        totalObjects = totalObjects,
        items = items?.map { it.toScanItem() },
        annotatedImageUrl = annotatedImageUrl,
        aiAnalysisUrl = aiAnalysis,
        pollution = pollutionData,
        impact = impactData,
        totalMassKg = totalMassKg,
        itemsMass = itemsMass?.map { it.toScanItemMass() },
        depthMapUrl = depthMapUrl,
        backendId = id,
    )
}

/** Adapt List<DetectTrashHistoryDto> (grouped by imageUrl) to ScanDetailData */
fun List<DetectTrashHistoryDto>.toScanDetailData(): ScanDetailData {
    val primary = minByOrNull { it.createdAt ?: "" } ?: first()

    // Check if it's "analyze_all" type (all data in one record)
    val analyzeAll = find { it.detectType == "analyze_all" }
    if (analyzeAll != null) {
        val pollutionData = analyzeAll.pollution?.toFlatMap()
        val impactData = analyzeAll.impact?.toScanImpact()
        return ScanDetailData(
            id = analyzeAll.id,
            imageUrl = analyzeAll.imageUrl,
            createdAt = analyzeAll.createdAt?.take(10) ?: "",
            scannedBy = analyzeAll.detectedBy?.fullName ?: analyzeAll.detectedBy?.username ?: "",
            status = parseWasteSortStatus(analyzeAll.status),
            totalObjects = analyzeAll.totalObjects ?: 0,
            items = analyzeAll.items?.map { it.toScanItem() },
            annotatedImageUrl = analyzeAll.annotatedImageUrl,
            aiAnalysisUrl = analyzeAll.aiAnalysis,
            pollution = pollutionData,
            impact = impactData,
            totalMassKg = analyzeAll.totalMassKg,
            itemsMass = analyzeAll.itemsMass?.map { it.toScanItemMass() },
            depthMapUrl = analyzeAll.depthMapUrl,
            backendId = analyzeAll.id,
        )
    }

    // Fallback: original logic for separate records
    val detectTrash = find { it.detectType == "detect_trash" }
    val pollutant = find { it.detectType == "predict_pollutant_impact" }
    val totalMass = find { it.detectType == "total_mass" }

    val pollutionData = pollutant?.pollution?.toFlatMap()
    val impactData = pollutant?.impact?.toScanImpact()

    return ScanDetailData(
        id = primary.id,
        imageUrl = primary.imageUrl,
        createdAt = primary.createdAt?.take(10) ?: "",
        scannedBy = primary.detectedBy?.fullName ?: primary.detectedBy?.username ?: "",
        status = groupStatus(this),
        totalObjects = detectTrash?.totalObjects ?: primary.totalObjects,
        items = detectTrash?.items?.map { it.toScanItem() },
        annotatedImageUrl = detectTrash?.annotatedImageUrl,
        aiAnalysisUrl = detectTrash?.aiAnalysis,
        pollution = pollutionData,
        impact = impactData,
        totalMassKg = totalMass?.totalMassKg,
        itemsMass = totalMass?.itemsMass?.map { it.toScanItemMass() },
        depthMapUrl = totalMass?.depthMapUrl,
        backendId = primary.id,
    )
}

/** Adapt WasteDetectResponse (from predict-pollutant API) to ScanDetailData */
fun WasteDetectResponse.toScanDetailData(backendId: String? = null): ScanDetailData {
    val impactData = impact
    val pollutionData = pollution
    return ScanDetailData(
        id = backendId ?: imageUrl.substringAfterLast("/").substringBeforeLast("."),
        imageUrl = imageUrl,
        createdAt = "",
        scannedBy = "",
        status = WasteSortStatus.SCANNED,
        totalObjects = totalObjects,
        items = items.map { ScanItem(it.name, it.quantity) },
        pollution = pollutionData,
        impact = impactData?.let { ScanImpact(it.airPollution, it.waterPollution, it.soilPollution) },
        backendId = backendId,
    )
}