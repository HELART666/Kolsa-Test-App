package com.example.kolsatestapp.fragments.video

import com.example.domain.model.video.Video
import com.example.domain.useCase.GetVideoByIdUseCase
import com.example.kolsatestapp.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(
    private val getVideoByIdUseCase: GetVideoByIdUseCase,
): BaseViewModel() {

    private val _videoUiState = MutableUIStateFlow<Video>()
    val videoUiState = _videoUiState.asStateFlow()

    private val _videoState = MutableStateFlow<Video?>(null)
    val videoState = _videoState.asStateFlow()

    fun getVideoById(id: Int) {
        getVideoByIdUseCase.getVideoById(id = id).collectNetworkRequest(_videoUiState)
    }

    fun onLoadVideo(video: Video) {
        _videoState.value = video
    }
}