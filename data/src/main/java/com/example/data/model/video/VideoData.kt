package com.example.data.model.video

import com.example.data.utils.Consts
import com.example.data.utils.DataMapper
import com.example.domain.model.video.Video
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoData(
    @SerialName("id")
    val id: Int,
    @SerialName("duration")
    val duration: Int,
    @SerialName("link")
    val link: String,
): DataMapper<Video> {
    override fun mapToDomain(): Video {
        val formattedLink = link.substringAfter('/')
        return Video(
            id = id,
            duration = duration,
            link = "${Consts.BASE_URL}$formattedLink",
        )
    }
}
