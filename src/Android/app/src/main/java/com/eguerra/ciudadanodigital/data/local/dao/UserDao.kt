package com.eguerra.ciudadanodigital.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.eguerra.ciudadanodigital.data.local.entity.UserModel

// Acciones en base de datos local
@Dao
interface UserDao {

    // Obtener detalle de usuario guardado según email
    @Query("SELECT * FROM UserModel WHERE email=:email LIMIT 1")
    suspend fun getUser(email: String): UserModel?

    // Obtener todos los usuarios almacenados localmente
    @Query("SELECT * FROM UserModel")
    suspend fun getUsers(): UserModel?

    // Insertar un nuevo usuario de forma local
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserModel)

    // Eliminar todos los usuarios almacenados de forma local.
    @Query("DELETE FROM UserModel")
    suspend fun deleteAll()
}