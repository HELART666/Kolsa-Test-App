package com.example.domain.repository

import com.example.domain.core.RemoteWrapper
import com.example.domain.model.video.Video

interface VideoRepository {

    fun getVideoById(id: Int): RemoteWrapper<Video>

}