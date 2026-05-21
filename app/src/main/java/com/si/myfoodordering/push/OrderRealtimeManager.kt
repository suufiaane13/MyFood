package com.si.myfoodordering.push

import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.si.myfoodordering.R
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRealtimeManager @Inject constructor(
    private val supabase: SupabaseClient,
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var listeningJob: Job? = null
    private var activeChannel: RealtimeChannel? = null
    private var currentUserId: String? = null

    fun startListening(userId: String) {
        if (currentUserId == userId && listeningJob?.isActive == true) return
        stopListening()
        currentUserId = userId

        val ch = supabase.channel("orders-$userId")
        activeChannel = ch

        val flow = ch.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "commandes"
        }

        listeningJob = scope.launch {
            ch.subscribe()
            flow.collect { action ->
                val record = action.record
                val recordUserId = record["user_id"]?.jsonPrimitive?.contentOrNull
                if (recordUserId != userId) return@collect
                val id = record["id"]?.jsonPrimitive?.intOrNull ?: return@collect
                val newStatut = record["statut"]?.jsonPrimitive?.contentOrNull ?: return@collect
                showStatusNotification(id, newStatut)
            }
        }
    }

    fun stopListening() {
        listeningJob?.cancel()
        listeningJob = null
        activeChannel?.let { ch ->
            scope.launch {
                try { ch.unsubscribe() } catch (_: Exception) {}
            }
        }
        activeChannel = null
        currentUserId = null
    }

    private fun showStatusNotification(orderId: Int, statut: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, NotificationChannels.CLIENT)
            .setSmallIcon(R.drawable.logo)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.logo))
            .setContentTitle("Commande #$orderId")
            .setContentText("Statut mis à jour : $statut")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        nm.notify(orderId, notification)
    }
}
