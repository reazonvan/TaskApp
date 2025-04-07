package com.example.taskapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var notificationScheduler: NotificationScheduler
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // После перезагрузки устройства переплланируем все уведомления
            CoroutineScope(Dispatchers.IO).launch {
                notificationScheduler.scheduleAllPendingNotifications()
            }
        }
    }
} 