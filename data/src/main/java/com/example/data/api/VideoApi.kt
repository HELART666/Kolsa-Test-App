package com.example.data.api

import com.example.data.model.video.VideoData
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

interface VideoApi {

    @GET("get_video")
    suspend fun getVideoById(@Query("id") id: Int): Response<VideoData>

    companion object {
        fun create(retrofit: Retrofit): VideoApi {
            return retrofit.create(VideoApi::class.java)
        }
    }
}