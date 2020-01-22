package pl.sbandurski.simpleradio.view.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import pl.sbandurski.simpleradio.R

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                resources.getString(R.string.CHANNEL_ID),
                "Simple radio channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.vibrationPattern = longArrayOf( 0 )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}