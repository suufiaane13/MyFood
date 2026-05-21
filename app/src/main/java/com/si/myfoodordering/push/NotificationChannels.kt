package com.si.myfoodordering.push

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {

    const val CLIENT = "commandes_client"
    const val ADMIN = "commandes_admin"

    fun ensure(application: Application) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(
                CLIENT,
                "Mes commandes",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "État de vos commandes" }
        )
        nm.createNotificationChannel(
            NotificationChannel(
                ADMIN,
                "Restaurant",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = "Nouvelles commandes et alertes admin" }
        )
    }
}
