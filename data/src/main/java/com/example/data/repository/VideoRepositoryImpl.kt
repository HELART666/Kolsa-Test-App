package com.example.data.repository

import com.example.data.api.VideoApi
import com.example.data.base.BaseRepository
import com.example.domain.core.RemoteWrapper
import com.example.domain.model.video.Video
import com.example.domain.repository.VideoRepository
import javax.inject.Inject

class VideoRepositoryImpl @Inject constructor(
    private val api: VideoApi,
) : BaseRepository(), VideoRepository {

    override fun getVideoById(id: Int): RemoteWrapper<Video> = doNetworkRequestWithMapping {
        api.getVideoById(id = id)
    }

}