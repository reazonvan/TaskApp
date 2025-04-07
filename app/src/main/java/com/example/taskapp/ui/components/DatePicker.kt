package com.example.taskapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePicker(
    selectedDate: Long?,
    onDateSelected: (Long?) -> Unit,
    label: String = "Срок сдачи"
) {
    val context = LocalContext.current
    val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
    
    var showDateDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }
    
    // Сохраняем выбранную дату и время в календаре
    val calendar = remember { Calendar.getInstance() }
    selectedDate?.let { 
        calendar.timeInMillis = it 
    } ?: calendar.setTimeInMillis(System.currentTimeMillis())
    
    var selectedHour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }

    OutlinedTextField(
        value = selectedDate?.let { dateTimeFormat.format(Date(it)) } ?: "Без срока",
        onValueChange = { },
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            Row {
                IconButton(onClick = { showTimeDialog = true }) {
                    Icon(Icons.Default.AccessTime, "Выбрать время")
                }
                IconButton(onClick = { showDateDialog = true }) {
                    Icon(Icons.Default.CalendarToday, "Выбрать дату")
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDateDialog = true }
    )
    
    if (showDateDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate ?: System.currentTimeMillis()
        )
        
        DatePickerDialog(
            onDismissRequest = { showDateDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDateMillis ->
                            // Сохраняем дату, но сохраняем старое время
                            calendar.timeInMillis = selectedDateMillis
                            calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                            calendar.set(Calendar.MINUTE, selectedMinute)
                            onDateSelected(calendar.timeInMillis)
                        }
                        showDateDialog = false
                        // Показываем диалог выбора времени после выбора даты
                        showTimeDialog = true
                    }
                ) {
                    Text("Выбрать время")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDateSelected(null)
                        showDateDialog = false
                    }
                ) {
                    Text("Без срока")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    if (showTimeDialog) {
        TimePickerDialog(
            onDismissRequest = { showTimeDialog = false },
            onConfirm = { hour, minute ->
                // Обновляем время в календаре
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                selectedHour = hour
                selectedMinute = minute
                onDateSelected(calendar.timeInMillis)
                showTimeDialog = false
            },
            initialHour = selectedHour,
            initialMinute = selectedMinute
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
    initialHour: Int,
    initialMinute: Int
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute
    )
    
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Выберите время") },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(timePickerState.hour, timePickerState.minute)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Отмена")
            }
        }
    )
} 