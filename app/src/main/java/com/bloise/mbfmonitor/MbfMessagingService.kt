package com.bloise.mbfmonitor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class MbfMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "mbf_emails"
        const val CHANNEL_NAME = "E-mails AWS"
        const val REGISTER_URL =
            "https://sx35x2e9pd.execute-api.us-east-1.amazonaws.com/prod/api/push/register"

        /** Envia o token FCM para o backend registrar. Chamado no onNewToken e no app. */
        fun registerToken(token: String) {
            Thread {
                try {
                    val url = URL(REGISTER_URL)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.doOutput = true
                    conn.connectTimeout = 10000
                    conn.readTimeout = 10000
                    val body = "{\"token\":\"$token\"}"
                    val os: OutputStream = conn.outputStream
                    os.write(body.toByteArray())
                    os.flush()
                    os.close()
                    conn.responseCode // dispara a requisição
                    conn.disconnect()
                } catch (_: Exception) {
                    // falha suave: tenta de novo no proximo ciclo/abertura
                }
            }.start()
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        registerToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: "MBF Monitor"
        val body = message.notification?.body ?: (message.data["body"] ?: "")
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Notificações de novos e-mails da AWS" }
            nm.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pi = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        nm.notify(System.currentTimeMillis().toInt(), notification)
    }
}
