package com.example.kolsatestapp.fragments.video

import androidx.annotation.OptIn
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.TrackSelectionDialogBuilder
import com.example.kolsatestapp.R
import com.example.kolsatestapp.base.BaseFragment
import com.example.kolsatestapp.databinding.FragmentVideoBinding
import com.example.kolsatestapp.fragments.utils.Consts
import dagger.hilt.android.AndroidEntryPoint
import dev.androidbroadcast.vbpd.viewBinding

@UnstableApi
@AndroidEntryPoint
class VideoFragment : BaseFragment<VideoViewModel, FragmentVideoBinding>(R.layout.fragment_video) {

    override val binding by viewBinding(FragmentVideoBinding::bind)
    override val viewModel: VideoViewModel by viewModels()
    private lateinit var trackSelector: DefaultTrackSelector
    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L

    override fun initialize() {
        super.initialize()
        arguments?.getInt(Consts.VIDEO_ID_KEY)?.let {
            viewModel.getVideoById(it)
        }
        setupSubscribers()

        binding.buttonQuality.setOnClickListener {
            showQualitySelector()
        }
    }

    override fun setupSubscribers() {
        super.setupSubscribers()
        subscribeToVideo()
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }

    @OptIn(UnstableApi::class)
    private fun initializePlayer(url: String) {
        trackSelector = DefaultTrackSelector(requireContext())
        player = ExoPlayer.Builder(requireActivity())
            .setMediaSourceFactory(configureDataSourceFactory())
            .build()
            .also { exoPlayer ->
                binding.videoView.player = exoPlayer
                val mediaItem = MediaItem.fromUri(url)
                exoPlayer.setMediaItems(listOf(mediaItem), currentItem, playbackPosition)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.prepare()
            }
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            currentItem = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.release()
        }
        player = null
    }

    @OptIn(UnstableApi::class)
    private fun configureDataSourceFactory(): DefaultMediaSourceFactory {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)

        val dataSourceFactory = DefaultDataSource.Factory(requireContext(), httpDataSourceFactory)

        return DefaultMediaSourceFactory(dataSourceFactory)
    }

    private fun subscribeToVideo() {
        viewModel.videoUiState.collectUIState(
            lifecycleState = Lifecycle.State.STARTED,
            onError = {
                it.setupErrors()
            },
            onSuccess = {
                initializePlayer(it.link)
            }
        )
    }

    // В вашей активности или фрагменте
    private fun showQualitySelector() {
        val trackSelectionDialogBuilder = TrackSelectionDialogBuilder(
            requireContext(),
            "Select Video Quality",
            player as Player,
            C.TRACK_TYPE_VIDEO
        )

        trackSelectionDialogBuilder
            .setAllowAdaptiveSelections(true)
            .build()
            .show()
    }
}