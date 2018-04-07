package com.andyisdope.cryptowatcher.database

import android.arch.persistence.room.TypeConverter
import java.util.*

class DateConverter {

    companion object {
        @TypeConverter
        fun fromTimeStamp(value: Long): Date {
            return Date(value)
        }

        @TypeConverter
        fun dateToTimeStamp(date: Date): Long {
            return date.time
        }
    }
}