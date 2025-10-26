package com.example.data.model.workout

import com.example.data.utils.DataMapper
import com.example.domain.model.workout.Workout
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object IntOrStringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("IntOrString", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }

    override fun deserialize(decoder: Decoder): String {
        return try {
            decoder.decodeInt().toString()
        } catch (e: Exception) {
            decoder.decodeString()
        }
    }
}

@kotlinx.serialization.Serializable
data class WorkoutData(
    @SerialName("id")
    val id: Int,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("type")
    val type: Int,
    @SerialName("duration")
    @Serializable(with = IntOrStringSerializer::class)
    val duration: String,
) : DataMapper<Workout> {
    override fun mapToDomain(): Workout {
        return Workout(
            id = id,
            title = title,
            description = description,
            type = type,
            duration = duration,
        )
    }
}
