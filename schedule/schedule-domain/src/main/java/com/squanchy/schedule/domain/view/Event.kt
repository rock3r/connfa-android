package com.squanchy.schedule.domain.view

import arrow.core.Option
import net.squanchy.eventdetails.domain.view.ExperienceLevel
import net.squanchy.speaker.domain.view.Speaker
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime

@Suppress("LongParameterList") // This is just a big model - TODO refactor this to split it up
data class Event(
    val id: String,
    val numericId: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val title: String,
    val place: Option<Place>,
    val track: Option<Track>,
    val speakers: List<Speaker>,
    val experienceLevel: Option<ExperienceLevel>,
    val type: Type,
    val favorite: Boolean,
    val description: Option<String>,
    val timeZone: DateTimeZone
) {

    fun isHappeningAt(time: LocalDateTime) = time.isAfter(startTime) && time.isBefore(endTime)

    enum class Type(private val rawType: String) {
        REGISTRATION("registration"),
        TALK("talk"),
        KEYNOTE("keynote"),
        COFFEE_BREAK("coffee_break"),
        LUNCH("lunch"),
        SOCIAL("social"),
        OTHER("other"),
        WORKSHOP("workshop");

        companion object {

            fun fromRawType(rawType: String): Type {
                return values().find { it.rawType.equals(rawType, ignoreCase = true) }
                    ?: throw IllegalArgumentException("Unsupported raw event type: $rawType")
            }
        }
    }
}
