package com.example.taskapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teachers")
data class Teacher(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // Фамилия И. О.
    val subject: String = "", // Название предмета
    val displayNameFirst: Boolean = false, // true - показывать название предмета первым, false - ФИО первым
    val createdAt: Long = System.currentTimeMillis()
) 