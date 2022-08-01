package uv.index.lib.data

import androidx.annotation.Keep
import io.ktor.client.request.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import uv.index.lib.net.CertOkHttpClient
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class UVIndexAPI(
    private val client: CertOkHttpClient,
    private val url: String,
    private val path: String,
    private val token: String
) {

    suspend fun get(latitude: Double, longitude: Double, date: ZonedDateTime): Result {
        return client.get(url + path) {
            parameter("lat", latitude)
            parameter("lon", longitude)
            parameter("dt", date.format(DateTimeFormatter.ISO_INSTANT))
            parameter("token", token)
        }
    }

    @Keep
    @Serializable
    data class Result(
        val status: String,
        val indices: List<Item>,
    )

    @Keep
    @Serializable
    data class Item(
        @Serializable(with = StringToZonedDateTimeSerializer::class)
        val dt: Instant,
        val uvi: Double
    )

    private object StringToZonedDateTimeSerializer: KSerializer<Instant> {

        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("dt", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Instant {
            return Instant.parse(decoder.decodeString())
        }

        override fun serialize(encoder: Encoder, value: Instant) {
            throw NotImplementedError()
        }

    }
}
