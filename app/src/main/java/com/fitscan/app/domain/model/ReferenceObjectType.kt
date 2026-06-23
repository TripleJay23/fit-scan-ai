package com.fitscan.app.domain.model

enum class ReferenceObjectType(
    val key: String,
    val displayName: String,
    val physicalWidthCm: Float,
    val useLongSideForPixels: Boolean
) {
    A4_PAPER(
        key = "a4",
        displayName = "A4 Paper",
        physicalWidthCm = 21.0f,
        useLongSideForPixels = false
    ),
    CREDIT_CARD(
        key = "credit_card",
        displayName = "Credit Card",
        physicalWidthCm = 8.56f,
        useLongSideForPixels = true
    );

    companion object {
        fun fromDisplayName(displayName: String): ReferenceObjectType? {
            return entries.firstOrNull { it.displayName == displayName }
        }
    }
}
