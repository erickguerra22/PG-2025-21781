package com.eguerra.ciudadanodigital.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DocumentModel(
    @PrimaryKey val documentId: Long,
    val userId: Long,
    val category: Int,
    val documentUrl: String,
    val title: String,
    val author: String,
    val year: Int
)