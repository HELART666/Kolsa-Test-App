package com.example.domain.useCase

import com.example.domain.repository.VideoRepository
import javax.inject.Inject

class GetVideoByIdUseCase @Inject constructor(
    private val repository: VideoRepository,
) {

    fun getVideoById(id: Int) = repository.getVideoById(id = id)

}