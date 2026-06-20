package com.fitscan.app.domain.model

data class ClothingSize(
    val category: String, // T-Shirt | Shirt | Trousers | Jacket
    val sizeText: String, // e.g. "L"
    val euSize: String, // e.g. "EU 50"
    val usSize: String, // e.g. "US 40"
    val ukSize: String, // e.g. "UK 40"
    val fitNotes: String
)
