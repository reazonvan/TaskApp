package com.example.taskapp.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.taskapp.data.model.Task
import com.example.taskapp.data.model.Teacher
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExportUtil {
    
    fun exportToTextFile(
        context: Context,
        teachers: List<Teacher>,
        tasksMap: Map<Long, List<Task>>
    ): Uri? {
        try {
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "taskapp_export_$timestamp.txt"
            
            val file = File(context.getExternalFilesDir(null), fileName)
            val fileOutputStream = FileOutputStream(file)
            
            val stringBuilder = StringBuilder()
            stringBuilder.append("ЭКСПОРТ ДАННЫХ TASKAPP\n")
            stringBuilder.append("Дата: ${dateFormat.format(Date())}\n\n")
            
            teachers.forEach { teacher ->
                stringBuilder.append("ПРЕПОДАВАТЕЛЬ: ${teacher.name}\n")
                stringBuilder.append("=".repeat(50) + "\n")
                
                val tasks = tasksMap[teacher.id] ?: emptyList()
                if (tasks.isEmpty()) {
                    stringBuilder.append("Нет задач\n")
                } else {
                    tasks.forEach { task ->
                        val status = if (task.isCompleted) "СДАНО" else "НЕ СДАНО"
                        stringBuilder.append("• ${task.title} - $status\n")
                        if (task.description.isNotBlank()) {
                            stringBuilder.append("  Описание: ${task.description}\n")
                        }
                        if (task.deadline != null) {
                            stringBuilder.append("  Срок: ${dateFormat.format(Date(task.deadline))}\n")
                        }
                        stringBuilder.append("\n")
                    }
                }
                stringBuilder.append("\n")
            }
            
            fileOutputStream.write(stringBuilder.toString().toByteArray())
            fileOutputStream.close()
            
            // Используем FileProvider для получения URI
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    fun shareFile(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Поделиться файлом"))
    }
} 