package com.fitscan.app.data.remote

import com.fitscan.app.data.remote.dto.AnalyzeResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FitScanApi {

    @Multipart
    @POST("/analyze")
    suspend fun analyzeImage(
        @Part imageFile: MultipartBody.Part,
        @Part("height_cm") heightCm: RequestBody
    ): Response<AnalyzeResponse>

}