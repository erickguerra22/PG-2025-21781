package com.eguerra.ciudadanodigital.helpers

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

class Converters {
    private val gson = Gson()

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromString(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, formatter) }
    }

    @TypeConverter
    fun localDateTimeToString(date: LocalDateTime?): String? {
        return date?.format(formatter)
    }

    @TypeConverter
    fun fromListOfLists(list: List<List<String>>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromStringToListOfLists(value: String): List<List<String>> {
        val listType = object : TypeToken<List<List<String>>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromMap(value: Map<String, Float>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toMap(value: String?): Map<String, Float>? {
        return value?.let {
            val type = object : TypeToken<Map<String, Float>>() {}.type
            gson.fromJson(it, type)
        }
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(separator = ",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }
}
