package com.eguerra.ciudadanodigital.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.eguerra.ciudadanodigital.data.local.dao.UserDao
import com.eguerra.ciudadanodigital.data.local.entity.UserModel
import com.eguerra.ciudadanodigital.helpers.Converters

@Database(
    entities = [UserModel::class],
    version = 2,
    exportSchema = false
)

@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {
    abstract fun userDao(): UserDao
}