package com.fitscan.app.ml

import com.fitscan.app.domain.model.ClothingSize
import com.fitscan.app.domain.model.BodyMeasurements

object SizeMapper {

    fun mapChestToSize(chestCirc: Float): Pair<String, String> {
        return when {
            chestCirc < 86f -> Pair("XS", "EU 44")
            chestCirc >= 86f && chestCirc < 92f -> Pair("S", "EU 46")
            chestCirc >= 92f && chestCirc < 98f -> Pair("M", "EU 48")
            chestCirc >= 98f && chestCirc < 104f -> Pair("L", "EU 50")
            chestCirc >= 104f && chestCirc < 110f -> Pair("XL", "EU 52")
            chestCirc >= 110f && chestCirc < 116f -> Pair("XXL", "EU 54")
            else -> Pair("XXXL", "EU 56")
        }
    }

    fun generateClothingSizes(measurements: BodyMeasurements): List<ClothingSize> {
        val (sizeText, euSize) = mapChestToSize(measurements.chestCirc)
        
        // Fit Notes based on shoulder vs chest ratio:
        // - If shoulderWidth > 47cm → "Broad shoulders — size up for jackets"
        // - If waistCirc < 70cm → "Slim waist — consider tailored fit"
        val generalFitNotes = when {
            measurements.shoulderWidth > 47f -> "Broad shoulders — consider sizing up for jackets to ensure full range of motion."
            measurements.waistCirc < 70f -> "Slim waist — consider tailored fit."
            else -> "Standard athletic fit. True to size."
        }

        val jacketFitNotes = if (measurements.shoulderWidth > 47f) {
            "Broad shoulders — size up for jackets"
        } else {
            generalFitNotes
        }

        val tShirt = ClothingSize(
            category = "T-Shirt",
            sizeText = sizeText,
            euSize = euSize,
            usSize = mapEuToUsUk(euSize),
            ukSize = mapEuToUsUk(euSize),
            fitNotes = generalFitNotes
        )

        val shirt = ClothingSize(
            category = "Shirt",
            sizeText = sizeText,
            euSize = euSize,
            usSize = mapEuToUsUk(euSize),
            ukSize = mapEuToUsUk(euSize),
            fitNotes = generalFitNotes
        )

        // Trousers
        val waistInches = Math.round(measurements.waistCirc / 2.54f)
        val inseamInches = Math.round((measurements.heightCm * 0.47f) / 2.54f) // typical 3D mapping ratio
        val trousers = ClothingSize(
            category = "Trousers",
            sizeText = "${waistInches}x${inseamInches}",
            euSize = "EU ${waistInches + 10}",
            usSize = "US ${waistInches}",
            ukSize = "UK ${waistInches}",
            fitNotes = if (measurements.waistCirc < 70f) "Slim waist — consider tailored fit" else "Standard fit"
        )

        val jacket = ClothingSize(
            category = "Jacket",
            sizeText = sizeText,
            euSize = euSize,
            usSize = mapEuToUsUk(euSize),
            ukSize = mapEuToUsUk(euSize),
            fitNotes = jacketFitNotes
        )

        return listOf(tShirt, shirt, trousers, jacket)
    }

    private fun mapEuToUsUk(eu: String): String {
        val digits = eu.replace("EU", "").trim().toIntOrNull() ?: 50
        val usUkSize = digits - 10
        return "US $usUkSize"
    }

    // Overloaded US/UK to display singular values correctly in row
    fun mapEuToUs(eu: String): String {
        val digits = eu.replace("EU", "").trim().toIntOrNull() ?: 50
        return "US ${digits - 10}"
    }

    fun mapEuToUk(eu: String): String {
        val digits = eu.replace("EU", "").trim().toIntOrNull() ?: 50
        return "UK ${digits - 10}"
    }
}
